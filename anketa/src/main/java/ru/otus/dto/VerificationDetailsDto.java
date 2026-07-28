package ru.otus.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record VerificationDetailsDto(
    Long staffAssignmentUserId,
    String name,
    String employeeName,
    String employeeUsername,
    String feedback,
    List<VerificationQuestionDto> questions,
    VerificationQuestionDto currentQuestion,
    Integer currentNumber,
    Integer verifiedQuestionsCount,
    Boolean canFinish
) {
}
