package ru.otus.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.otus.dto.AssignedStaffEvaluationDto;
import ru.otus.dto.StaffEvaluationResultDto;
import ru.otus.dto.EvaluationResultQuestionDto;
import ru.otus.entity.StaffEvaluationAnswer;
import ru.otus.entity.StaffEvaluationQuestion;
import ru.otus.entity.StaffEvaluationUser;
import ru.otus.repositories.StaffEvaluationAnswerRepository;
import ru.otus.repositories.StaffEvaluationQuestionRepository;
import ru.otus.repositories.StaffEvaluationUserRepository;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static ru.otus.entity.enums.AnswerResponse.NO;
import static ru.otus.entity.enums.AnswerResponse.YES;
import static ru.otus.entity.enums.StaffEvaluationStatus.ACTIVE;
import static ru.otus.entity.enums.StaffEvaluationStatus.DRAFT;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.FEEDBACK;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.IN_PROGRESS;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.NEW;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.VERIFICATION;
import static ru.otus.exceptions.WebApplicationException.errorOf;

@Service
@RequiredArgsConstructor
public class StaffEvaluationUserServiceImpl implements StaffEvaluationUserService {

    private static final String DATE_FROM = "staffEvaluation.dateFrom";

    private final StaffEvaluationUserRepository staffEvaluationUserRepository;

    private final StaffEvaluationQuestionRepository staffEvaluationQuestionRepository;

    private final StaffEvaluationAnswerRepository staffEvaluationAnswerRepository;

    private final MessageSource messageSource;

    @Override
    public Page<AssignedStaffEvaluationDto> findAssigned(Long userId, Pageable pageable) {
        var pageRequest = PageRequest.of(pageable.getPageNumber(),pageable.getPageSize(), DESC, DATE_FROM);
        return staffEvaluationUserRepository
            .findByUserIdAndStaffEvaluationStatusNot(userId, DRAFT, pageRequest)
            .map(this::dtoOf);
    }

    @Override
    public List<AssignedStaffEvaluationDto> findActive(Long userId) {
        return staffEvaluationUserRepository
            .findByUserIdAndStaffEvaluationStatus(userId, ACTIVE).stream()
            .filter(it -> List.of(NEW, IN_PROGRESS, FEEDBACK, VERIFICATION).contains(it.getStatus()))
            .map(this::dtoOf)
            .toList();
    }

    @Override
    public StaffEvaluationResultDto findResult(Long userId, Long staffEvaluationId) {
        var staffEvaluationUser = getStaffEvaluationUser(userId, staffEvaluationId);
        var questionositionMap = questionPositionMapOf(staffEvaluationUser);
        var questions = staffEvaluationAnswerRepository.findByStaffEvaluationUserId(staffEvaluationUser.getId()).stream()
            .sorted(comparing(answer -> questionositionMap.getOrDefault(answer.getQuestion().getId(), MAX_VALUE)))
            .map(answer -> resultQuestionOf(answer, questionositionMap.get(answer.getQuestion().getId())))
            .toList();
        return StaffEvaluationResultDto.builder()
            .assignmentId(staffEvaluationUser.getId())
            .name(staffEvaluationUser.getStaffEvaluation().getName())
            .dateFrom(staffEvaluationUser.getStaffEvaluation().getDateFrom())
            .dateTo(staffEvaluationUser.getStaffEvaluation().getDateTo())
            .status(staffEvaluationUser.getStatus())
            .verifiedBy(staffEvaluationUser.getVerifiedBy() != null ?
                staffEvaluationUser.getVerifiedBy().getDisplayName()
                : StringUtils.EMPTY
            ).verifiedAnswers((int) questions.stream().filter(question -> question.verifiedResponse() != null).count())
            .matchedAnswers((int) questions.stream().filter(question -> YES == question.verifiedResponse()).count())
            .mismatchedAnswers((int) questions.stream().filter(question -> NO == question.verifiedResponse()).count())
            .questions(questions)
            .build();
    }

    @Nonnull
    private StaffEvaluationUser getStaffEvaluationUser(Long userId, Long staffEvaluationId) {
        return staffEvaluationUserRepository.findByStaffEvaluationIdAndUserId(staffEvaluationId, userId)
            .orElseThrow(() -> errorOf(
                NOT_FOUND,
                messageSource.getMessage("error.staff-evaluation-not-found", new Object[]{}, getLocale()))
            );
    }

    private AssignedStaffEvaluationDto dtoOf(StaffEvaluationUser assignment) {
        var evaluation = assignment.getStaffEvaluation();
        return new AssignedStaffEvaluationDto(
            assignment.getId(),
            evaluation.getId(),
            evaluation.getName(),
            evaluation.getDateFrom(),
            evaluation.getDateTo(),
            evaluation.getStatus(),
            assignment.getUser().getProjectRole() != null
                ? assignment.getUser().getProjectRole().getCode()
                : null,
            assignment.getStatus()
        );
    }

    private Map<Long, Integer> questionPositionMapOf(StaffEvaluationUser staffEvaluationUser) {
        var projectRole = staffEvaluationUser.getUser().getProjectRole();
        if (projectRole == null) {
            return Map.of();
        }
        return staffEvaluationQuestionRepository
            .findByStaffEvaluationIdAndQuestionProjectRoleIdOrderByPositionAsc(
                staffEvaluationUser.getStaffEvaluation().getId(),
                projectRole.getId()
            ).stream()
            .collect(toMap(question -> question.getQuestion().getId(), StaffEvaluationQuestion::getPosition));
    }

    private EvaluationResultQuestionDto resultQuestionOf(StaffEvaluationAnswer staffEvaluationAnswer, Integer position) {
        var question = staffEvaluationAnswer.getQuestion();
        return EvaluationResultQuestionDto.builder()
            .position(position)
            .areaKnowledge(question.getAreaKnowledge())
            .section(question.getSection())
            .question(question.getText())
            .response(staffEvaluationAnswer.getResponse())
            .verifiedResponse(staffEvaluationAnswer.getVerifiedResponse())
            .verificationComment(staffEvaluationAnswer.getVerificationComment())
            .build();
    }
}
