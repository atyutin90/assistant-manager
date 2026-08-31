package ru.otus.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record VerificationDetailsDto(
    Long staffAssignmentUserId,
    String name,
    LocalDate dateFrom,
    LocalDate dateTo,
    String employeeName,
    String employeeUsername,
    String projectRole,
    String feedback,
    List<VerificationQuestionDto> questions,
    Integer verifiedQuestionsCount,
    Boolean canFinish
) {
}
