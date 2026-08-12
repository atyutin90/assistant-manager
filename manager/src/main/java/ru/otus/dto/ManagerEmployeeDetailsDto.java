package ru.otus.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ManagerEmployeeDetailsDto(
    Long employeeId,
    String fullName,
    String username,
    String email,
    String projectRole,
    Long projectId,
    Long staffEvaluationId,
    String verifiedBy,
    int answerCount,
    int matchedAnswerCount,
    int mismatchedAnswerCount,
    int pendingAnswerCount,
    List<ManagerSkillAnswersDto> skills
) {
}
