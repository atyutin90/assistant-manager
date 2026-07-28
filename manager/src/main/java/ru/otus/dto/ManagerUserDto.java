package ru.otus.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ManagerUserDto(
    Long id,
    String lastName,
    String middleName,
    String firstName,
    String username,
    String email,
    Long projectRoleId,
    String projectRole,
    List<ManagerSkillLevelDto> skillLevels
) { }
