package ru.otus.services.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.dto.VerificationDetailsDto;
import ru.otus.dto.VerificationFormDto;
import ru.otus.dto.VerificationItemDto;
import ru.otus.dto.VerificationQuestionDto;
import ru.otus.entity.StaffEvaluationAnswer;
import ru.otus.entity.StaffEvaluationQuestion;
import ru.otus.entity.StaffEvaluationUser;
import ru.otus.exceptions.WebApplicationException;
import ru.otus.repositories.StaffEvaluationAnswerRepository;
import ru.otus.repositories.StaffEvaluationQuestionRepository;
import ru.otus.repositories.StaffEvaluationUserRepository;

import java.util.List;
import java.util.Map;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.COMPLETED;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.VERIFICATION;
import static ru.otus.entity.enums.AnswerResponse.YES;
import static ru.otus.exceptions.WebApplicationException.errorOf;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final StaffEvaluationUserRepository staffEvaluationUserRepository;

    private final StaffEvaluationQuestionRepository staffEvaluationQuestionRepository;

    private final StaffEvaluationAnswerRepository staffEvaluationAnswerRepository;

    private final MessageSource messageSource;

    @Override
    public Page<VerificationItemDto> findPending(Long userId, Pageable pageable) {
        return staffEvaluationUserRepository
            .findByUserResponsiblesIdAndStatus(userId, VERIFICATION, pageable)
            .map(staffEvaluationUser -> listItemOf(staffEvaluationUser, userId));
    }

    @Override
    public VerificationDetailsDto findDetails(Long staffEvaluationUserId, Long verifierId) {
        var staffEvaluationUser = findStaffEvaluationUser(staffEvaluationUserId, verifierId);
        var positions = questionPositionMapOf(staffEvaluationUser);
        var answers = sortedAnswers(staffEvaluationUserId, positions);
        checkAnswers(answers);
        var verifiedQuestionCount = countVerifiedQuestions(answers);
        var editable = isOwnedBy(staffEvaluationUser, verifierId);
        return VerificationDetailsDto.builder()
            .staffAssignmentUserId(staffEvaluationUser.getId())
            .name(staffEvaluationUser.getStaffEvaluation().getName())
            .dateFrom(staffEvaluationUser.getStaffEvaluation().getDateFrom())
            .dateTo(staffEvaluationUser.getStaffEvaluation().getDateTo())
            .employeeName(staffEvaluationUser.getUser().getDisplayName())
            .employeeUsername(staffEvaluationUser.getUser().getUsername())
            .projectRole(staffEvaluationUser.getProjectRole() != null
                ? staffEvaluationUser.getProjectRole().getName() : null)
            .verificationOwner(ownerNameOf(staffEvaluationUser))
            .edit(editable)
            .canTake(!editable)
            .feedback(staffEvaluationUser.getFeedbackMessage())
            .questions(questionsOf(answers, positions))
            .verifiedQuestionsCount(verifiedQuestionCount)
            .canFinish(editable && verifiedQuestionCount == answers.size())
            .build();
    }

    @Override
    @Transactional
    public void take(Long staffEvaluationUserId, Long verifierId) {
        var staffEvaluationUser = findStaffEvaluationUserForUpdate(staffEvaluationUserId, verifierId);
        var verifier = staffEvaluationUser.getUser().getResponsibles().stream()
            .filter(responsible -> responsible.getId().equals(verifierId))
            .filter(responsible -> responsible.getProjectRoles().stream().anyMatch(projectRole ->
                projectRole.getId().equals(staffEvaluationUser.getProjectRole().getId())))
            .findFirst()
            .orElseThrow(this::notFoundStaffEvaluationUser);
        staffEvaluationUser.setVerificationOwner(verifier);
        staffEvaluationUserRepository.save(staffEvaluationUser);
    }

    @Override
    @Transactional
    public void save(Long staffEvaluationUserId, Long verifierId, VerificationFormDto form) {
        findOwnedStaffEvaluationUserForUpdate(staffEvaluationUserId, verifierId);
        var answers = staffEvaluationAnswerRepository.findByStaffEvaluationUserId(staffEvaluationUserId);
        var answer = answers.stream()
            .filter(value -> value.getId().equals(form.answerId()))
            .findFirst()
            .orElseThrow(this::answerNotFound);
        answer.setVerifiedResponse(form.response());
        answer.setVerificationComment(normalizeComment(form.comment()));
        staffEvaluationAnswerRepository.save(answer);
    }

    @Override
    @Transactional
    public void confirmAll(Long staffEvaluationUserId, Long verifierId) {
        findOwnedStaffEvaluationUserForUpdate(staffEvaluationUserId, verifierId);
        var answers = staffEvaluationAnswerRepository.findByStaffEvaluationUserId(staffEvaluationUserId);
        checkAnswers(answers);
        answers.forEach(answer -> answer.setVerifiedResponse(YES));
        staffEvaluationAnswerRepository.saveAll(answers);
    }

    @Override
    @Transactional
    public void complete(Long assignmentId, Long verifierId) {
        var assignment = findOwnedStaffEvaluationUserForUpdate(assignmentId, verifierId);
        var answers = staffEvaluationAnswerRepository.findByStaffEvaluationUserId(assignmentId);
        completeVerification(assignment, answers);
    }

    private List<VerificationQuestionDto> questionsOf(List<StaffEvaluationAnswer> staffEvaluationAnswers,
                                                      Map<Long, Integer> positions) {
        return staffEvaluationAnswers.stream()
            .map(answer -> questionOf(answer, positions.get(answer.getQuestion().getId())))
            .toList();
    }

    private List<StaffEvaluationAnswer> sortedAnswers(Long assignmentId, Map<Long, Integer> positions) {
        return staffEvaluationAnswerRepository.findByStaffEvaluationUserId(assignmentId).stream()
            .sorted(comparing(answer -> positions.getOrDefault(answer.getQuestion().getId(), MAX_VALUE)))
            .toList();
    }

    private int countVerifiedQuestions(List<StaffEvaluationAnswer> staffEvaluationAnswers) {
        return (int) staffEvaluationAnswers.stream()
            .filter(answer -> answer.getVerifiedResponse() != null)
            .count();
    }

    private void checkAnswers(List<StaffEvaluationAnswer> staffEvaluationAnswers) {
        if (staffEvaluationAnswers.isEmpty()) {
            throw errorOf(
                NOT_FOUND,
                messageSource.getMessage("error.answers-not-found-for-verification", new Object[]{}, getLocale())
            );
        }
    }

    private void completeVerification(StaffEvaluationUser staffEvaluationUser,
                                      List<StaffEvaluationAnswer> answers) {
        if (answers.isEmpty() || answers.stream().anyMatch(answer -> isNull(answer.getVerifiedResponse()))) {
            throw errorOf(
                CONFLICT,
                messageSource.getMessage("error.verify-all-answers-before-completing", new Object[]{}, getLocale())
            );
        }
        staffEvaluationUser.setStatus(COMPLETED);
        staffEvaluationUser.setVerifiedBy(staffEvaluationUser.getVerificationOwner());
        staffEvaluationUser.setVerificationOwner(null);
        staffEvaluationUserRepository.save(staffEvaluationUser);
    }

    private StaffEvaluationUser findStaffEvaluationUser(Long userId, Long verifierId) {
        return staffEvaluationUserRepository
            .findByIdAndUserResponsiblesIdAndStatus(userId, verifierId, VERIFICATION)
            .orElseThrow(this::notFoundStaffEvaluationUser);
    }

    private StaffEvaluationUser findStaffEvaluationUserForUpdate(Long userId, Long verifierId) {
        return staffEvaluationUserRepository
            .findForUpdateByIdAndUserResponsiblesIdAndStatus(userId, verifierId, VERIFICATION)
            .orElseThrow(this::notFoundStaffEvaluationUser);
    }

    private StaffEvaluationUser findOwnedStaffEvaluationUserForUpdate(Long userId, Long verifierId) {
        var assignment = findStaffEvaluationUserForUpdate(userId, verifierId);
        if (!isOwnedBy(assignment, verifierId)) {
            throw verificationNotOwned();
        }
        return assignment;
    }

    private boolean isOwnedBy(StaffEvaluationUser staffEvaluationUser, Long verifierId) {
        return staffEvaluationUser.getVerificationOwner() != null
            && staffEvaluationUser.getVerificationOwner().getId().equals(verifierId);
    }

    private String ownerNameOf(StaffEvaluationUser staffEvaluationUser) {
        return staffEvaluationUser.getVerificationOwner() != null
            ? staffEvaluationUser.getVerificationOwner().getDisplayName()
            : null;
    }

    private Map<Long, Integer> questionPositionMapOf(StaffEvaluationUser staffEvaluationUser) {
        var projectRole = staffEvaluationUser.getProjectRole();
        if (projectRole == null) {
            return Map.of();
        }
        return staffEvaluationQuestionRepository
            .findByStaffEvaluationIdAndQuestionProjectRoleIdOrderByPositionAsc(
                staffEvaluationUser.getStaffEvaluation().getId(), projectRole.getId()).stream()
            .collect(toMap(question -> question.getQuestion().getId(), StaffEvaluationQuestion::getPosition));
    }

    private VerificationItemDto listItemOf(StaffEvaluationUser assignment, Long verifierId) {
        var staffEvaluation = assignment.getStaffEvaluation();
        var ownedByCurrentVerifier = isOwnedBy(assignment, verifierId);
        return VerificationItemDto.builder()
            .staffEvaluationUserId(assignment.getId())
            .name(staffEvaluation.getName())
            .dateFrom(staffEvaluation.getDateFrom())
            .dateTo(staffEvaluation.getDateTo())
            .employeeName(assignment.getUser().getDisplayName())
            .employeeUsername(assignment.getUser().getUsername())
            .projectRole(assignment.getProjectRole() != null ? assignment.getProjectRole().getName() : null)
            .verificationOwner(ownerNameOf(assignment))
            .ownedByCurrentVerifier(ownedByCurrentVerifier)
            .canTake(!ownedByCurrentVerifier)
            .staffEvaluationStatus(staffEvaluation.getStatus())
            .staffEvaluationUserStatus(assignment.getStatus())
            .build();
    }

    private VerificationQuestionDto questionOf(StaffEvaluationAnswer staffEvaluationAnswer, Integer position) {
        var question = staffEvaluationAnswer.getQuestion();
        return VerificationQuestionDto.builder()
            .answerId(staffEvaluationAnswer.getId())
            .uuid(question.getUuid())
            .position(position)
            .areaKnowledge(question.getAreaKnowledge())
            .section(question.getSection())
            .question(question.getText())
            .employeeResponse(staffEvaluationAnswer.getResponse())
            .verifiedResponse(staffEvaluationAnswer.getVerifiedResponse())
            .comment(staffEvaluationAnswer.getVerificationComment())
            .build();
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        return comment.trim();
    }

    private WebApplicationException notFoundStaffEvaluationUser() {
        return errorOf(
            NOT_FOUND,
            messageSource.getMessage("error.staff-evaluation-user-not-found", new Object[]{}, getLocale())
        );
    }

    private WebApplicationException answerNotFound() {
        return errorOf(
            NOT_FOUND,
            messageSource.getMessage("error.answer-not-belong-survey", new Object[]{},
                getLocale())
        );
    }

    private WebApplicationException verificationNotOwned() {
        return errorOf(
            CONFLICT,
            messageSource.getMessage("error.verification-not-owned", new Object[]{}, getLocale())
        );
    }
}
