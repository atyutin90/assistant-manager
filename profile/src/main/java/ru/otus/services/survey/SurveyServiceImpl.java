package ru.otus.services.survey;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.dto.FeedbackFormDto;
import ru.otus.dto.SurveyAnswerCommand;
import ru.otus.dto.SurveyPageDto;
import ru.otus.dto.SurveyQuestionDto;
import ru.otus.entity.StaffEvaluationAnswer;
import ru.otus.entity.Question;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.StaffEvaluationQuestion;
import ru.otus.entity.StaffEvaluationUser;
import ru.otus.entity.enums.AnswerResponse;
import ru.otus.exceptions.WebApplicationException;
import ru.otus.repositories.StaffEvaluationAnswerRepository;
import ru.otus.repositories.StaffEvaluationQuestionRepository;
import ru.otus.repositories.StaffEvaluationUserRepository;

import java.util.List;
import java.util.Map;

import static java.util.List.of;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static ru.otus.entity.enums.StaffEvaluationStatus.ACTIVE;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.FEEDBACK;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.IN_PROGRESS;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.NEW;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.VERIFICATION;
import static ru.otus.exceptions.WebApplicationException.errorOf;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {

    private final StaffEvaluationUserRepository staffEvaluationUserRepository;

    private final StaffEvaluationQuestionRepository staffEvaluationQuestionRepository;

    private final StaffEvaluationAnswerRepository answerRepository;

    private final MessageSource messageSource;

    @Override
    public String findStartQuestionUuid(Long staffEvaluationId, Long userId, String projectRole) {
        var context = surveyContextOf(staffEvaluationId, projectRole, userId);
        checkStatus(context);
        var answerMap = answerMapOf(context.staffEvaluationUser());
        return context.questions().stream()
            .filter(question -> !answerMap.containsKey(question.getQuestion().getId()))
            .findFirst()
            .or(() -> context.questions().stream().findFirst())
            .map(question -> question.getQuestion().getUuid())
            .orElseThrow(this::questionNotFoundForProjectRole);
    }

    @Override
    public SurveyPageDto findQuestion(Long staffEvaluationId, Long userId, String projectRole, String uuid) {
        var context = surveyContextOf(staffEvaluationId, projectRole, userId);
        checkStatus(context);
        var questions = surveyQuestionsOf(context);
        var currentQuestion = questions.stream()
            .filter(question -> question.uuid().equals(uuid))
            .findFirst()
            .orElseThrow(this::questionNotFound);
        return pageOf(staffEvaluationId, context, questions, currentQuestion);
    }

    @Override
    @Transactional
    public void saveAnswer(SurveyAnswerCommand command) {
        var context = surveyContextOf(command.staffEvaluationId(), command.projectRole(), command.userId());
        if (cannotAnswer(context.staffEvaluationUser())) {
            throw staffEvaluationIsCompleted();
        }
        var evaluationQuestion = context.questions().stream()
            .filter(question -> question.getQuestion().getUuid().equals(command.questionUuid()))
            .findFirst()
            .orElseThrow(this::questionNotFound);
        saveAnswer(context.staffEvaluationUser(), evaluationQuestion, command.response());
    }

    @Override
    public void complete(Long staffEvaluationId, Long userId, String projectRole) {
        var context = surveyContextOf(staffEvaluationId, projectRole, userId);
        finishSurvey(context);
    }

    @Override
    public FeedbackFormDto findFeedback(Long staffEvaluationUserId, Long userId) {
        var staffEvaluationUser = activeStaffEvaluationUserById(staffEvaluationUserId, userId);
        checkFeedbackStatus(staffEvaluationUser);
        return FeedbackFormDto.builder()
            .message(staffEvaluationUser.getFeedbackMessage())
            .build();
    }

    @Override
    @Transactional
    public void saveFeedback(
        Long staffEvaluationUserId,
        Long userId,
        FeedbackFormDto feedback,
        boolean finish
    ) {
        var staffEvaluationUser = activeStaffEvaluationUserById(staffEvaluationUserId, userId);
        checkFeedbackStatus(staffEvaluationUser);
        staffEvaluationUser.setFeedbackMessage(feedback.message());
        if (finish) {
            staffEvaluationUser.setStatus(VERIFICATION);
        }
        staffEvaluationUserRepository.save(staffEvaluationUser);
    }

    private SurveyPageDto pageOf(
        Long evaluationId,
        SurveyContext context,
        List<SurveyQuestionDto> questions,
        SurveyQuestionDto currentQuestion
    ) {
        var questionIds = context.questions().stream()
            .map(question -> question.getQuestion().getId())
            .collect(java.util.stream.Collectors.toSet());
        var canFinish = context.staffEvaluationUser().getAnswers().stream()
            .filter(it -> it.getResponse() != null)
            .filter(it -> questionIds.contains(it.getQuestion().getId()))
            .count() == questions.size();
        return SurveyPageDto.builder()
            .staffEvaluationId(evaluationId)
            .evaluationName(context.staffEvaluationUser().getStaffEvaluation().getName())
            .projectRole(context.projectRole().getCode())
            .questions(questions)
            .currentQuestion(currentQuestion)
            .currentNumber(questions.indexOf(currentQuestion) + 1)
            .canFinish(canFinish)
            .build();
    }

    private void saveAnswer(
        StaffEvaluationUser assignmentUser,
        StaffEvaluationQuestion evaluationQuestion,
        AnswerResponse response
    ) {
        var question = evaluationQuestion.getQuestion().getId();
        var answer = answerRepository.findByStaffEvaluationUserIdAndQuestionId(assignmentUser.getId(), question)
            .orElseGet(StaffEvaluationAnswer::new);
        answer.setResponse(response);
        answer.setQuestion(evaluationQuestion.getQuestion());
        answer.setStaffEvaluationUser(assignmentUser);
        answerRepository.save(answer);
        if (assignmentUser.getStatus() == NEW) {
            assignmentUser.setStatus(IN_PROGRESS);
            staffEvaluationUserRepository.save(assignmentUser);
        }
    }

    private void finishSurvey(SurveyContext context) {
        long answeredQuestions = answerRepository.findByStaffEvaluationUserId(context.staffEvaluationUser().getId())
            .stream()
            .map(StaffEvaluationAnswer::getQuestion)
            .map(Question::getId)
            .filter(questionId -> context.questions().stream()
                .anyMatch(question -> question.getQuestion().getId().equals(questionId)))
            .distinct()
            .count();
        if (answeredQuestions != context.questions().size()) {
            throw errorOf(
                NOT_FOUND,
                messageSource.getMessage(
                    "error.answer-all-questions-before-completing-survey", new Object[]{}, getLocale())
            );
        }
        context.staffEvaluationUser().setStatus(FEEDBACK);
        staffEvaluationUserRepository.save(context.staffEvaluationUser());
    }

    private SurveyContext surveyContextOf(Long staffEvaluationId, String projectRole, Long userId) {
        var staffEvaluationUser = activeStaffEvaluationUserOf(staffEvaluationId, userId, projectRole);
        var assignedRole = staffEvaluationUser.getProjectRole();
        if (assignedRole == null || !assignedRole.getCode().equalsIgnoreCase(projectRole)) {
            throw errorOf(
                NOT_FOUND,
                messageSource.getMessage("error.staff-evaluation-not-found", new Object[]{}, getLocale()));
        }
        var questions = staffEvaluationQuestionRepository
            .findByStaffEvaluationIdAndQuestionProjectRoleIdOrderByPositionAsc(staffEvaluationId, assignedRole.getId());
        return SurveyContext.builder()
            .staffEvaluationUser(staffEvaluationUser)
            .projectRole(assignedRole)
            .questions(questions)
            .build();
    }

    private StaffEvaluationUser activeStaffEvaluationUserOf(Long staffEvaluationId, Long userId, String projectRole) {
        return staffEvaluationUserRepository
            .findByStaffEvaluationIdAndUserIdAndProjectRoleCodeIgnoreCase(staffEvaluationId, userId, projectRole)
            .filter(it -> ACTIVE.equals(it.getStaffEvaluation().getStatus()))
            .orElseThrow(() ->
                errorOf(
                    NOT_FOUND,
                    messageSource.getMessage("error.staff-evaluation-not-found", new Object[]{}, getLocale()))
            );
    }

    private StaffEvaluationUser activeStaffEvaluationUserById(Long staffEvaluationUserId, Long userId) {
        return staffEvaluationUserRepository.findByIdAndUserId(staffEvaluationUserId, userId)
            .filter(it -> ACTIVE.equals(it.getStaffEvaluation().getStatus()))
            .orElseThrow(() -> errorOf(
                NOT_FOUND,
                messageSource.getMessage("error.staff-evaluation-not-found", new Object[]{}, getLocale())
            ));
    }

    private Map<Long, StaffEvaluationAnswer> answerMapOf(StaffEvaluationUser staffEvaluationUserId) {
        return answerRepository.findByStaffEvaluationUserId(staffEvaluationUserId.getId()).stream()
            .collect(toMap(answer -> answer.getQuestion().getId(), identity()));
    }

    private List<SurveyQuestionDto> surveyQuestionsOf(SurveyContext context) {
        var answers = answerMapOf(context.staffEvaluationUser());
        return context.questions().stream()
            .map(question -> questionOf(question, answers.get(question.getQuestion().getId())))
            .toList();
    }

    private SurveyQuestionDto questionOf(StaffEvaluationQuestion staffEvaluationQuestion,
                                         StaffEvaluationAnswer staffEvaluationAnswer) {
        var question = staffEvaluationQuestion.getQuestion();
        return SurveyQuestionDto.builder()
            .uuid(question.getUuid())
            .areaKnowledge(question.getAreaKnowledge())
            .section(question.getSection())
            .text(question.getText())
            .position(staffEvaluationQuestion.getPosition())
            .response(staffEvaluationAnswer != null ? staffEvaluationAnswer.getResponse() : null)
            .build();
    }

    private void checkFeedbackStatus(StaffEvaluationUser assignment) {
        if (!FEEDBACK.equals(assignment.getStatus())) {
            throw errorOf(
                CONFLICT,
                messageSource.getMessage("error.feedback-not-available", new Object[]{}, getLocale())
            );
        }
    }

    private void checkStatus(SurveyContext context) {
        if (cannotAnswer(context.staffEvaluationUser())) {
            throw staffEvaluationIsCompleted();
        }
    }

    private boolean cannotAnswer(StaffEvaluationUser assignment) {
        return !of(NEW, IN_PROGRESS).contains(assignment.getStatus());
    }

    private WebApplicationException staffEvaluationIsCompleted() {
        return errorOf(
            CONFLICT,
            messageSource.getMessage("error.staff-evaluation-is-completed", new Object[]{}, getLocale())
        );
    }

    private WebApplicationException questionNotFoundForProjectRole() {
        return errorOf(
            NOT_FOUND,
            messageSource.getMessage("error.questions-not-found-for-this-project-role", new Object[]{},
                getLocale())
        );
    }

    private WebApplicationException questionNotFound() {
        return errorOf(
            NOT_FOUND,
            messageSource.getMessage("error.question-not-found", new Object[]{},
                getLocale())
        );
    }

    @Builder
    private record SurveyContext(
        StaffEvaluationUser staffEvaluationUser,
        ProjectRole projectRole,
        List<StaffEvaluationQuestion> questions
    ) {
    }
}
