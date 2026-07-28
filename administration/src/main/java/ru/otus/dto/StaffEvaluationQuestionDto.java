package ru.otus.dto;

import lombok.Builder;

@Builder
public record StaffEvaluationQuestionDto(
    Long id,
    Boolean enabled,
    String uuid,
    Long projectRole,
    Long skill,
    String areaKnowledge,
    String section,
    String text,
    Integer position
) { }
