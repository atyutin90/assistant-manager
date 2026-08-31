package ru.otus.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ManagerEmployeeDto(
    Long id,
    String lastName,
    String middleName,
    String firstName,
    String username,
    String email,
    Long projectRoleId,
    String projectRole,
    String projectRoleCode,
    Long staffEvaluationId,
    List<ManagerSkillLevelDto> skillLevels
) { }
