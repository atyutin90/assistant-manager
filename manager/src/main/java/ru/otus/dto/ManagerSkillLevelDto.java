package ru.otus.dto;

import lombok.Builder;

@Builder
public record ManagerSkillLevelDto(
    Long skillId,
    String skill,
    Long careerLevelId,
    String careerLevel
) { }
