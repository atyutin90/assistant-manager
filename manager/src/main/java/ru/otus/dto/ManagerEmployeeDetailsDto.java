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
    Long projectRoleId,
    Long projectId,
    Long staffEvaluationId,
    String verifiedBy,
    String feedback,
    int answerCount,
    int matchedAnswerCount,
    int mismatchedAnswerCount,
    List<ManagerSkillAnswersDto> skills
) {
}
