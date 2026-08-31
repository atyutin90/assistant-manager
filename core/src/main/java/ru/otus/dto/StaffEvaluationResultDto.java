package ru.otus.dto;

import lombok.Builder;
import ru.otus.entity.enums.StaffEvaluationUserStatus;

import java.time.LocalDate;
import java.util.List;

@Builder
public record StaffEvaluationResultDto(
    Long assignmentId,
    String name,
    LocalDate dateFrom,
    LocalDate dateTo,
    StaffEvaluationUserStatus status,
    String projectRole,
    String verifiedBy,
    int verifiedAnswers,
    int matchedAnswers,
    int mismatchedAnswers,
    List<EvaluationResultQuestionDto> questions
) {
}
