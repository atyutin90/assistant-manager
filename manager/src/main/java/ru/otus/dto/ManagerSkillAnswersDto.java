package ru.otus.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ManagerSkillAnswersDto(
    Long skillId,
    String skill,
    int answerCount,
    String calculatedCareerLevel,
    List<ManagerCareerLevelAnswersDto> careerLevels
) {
}
