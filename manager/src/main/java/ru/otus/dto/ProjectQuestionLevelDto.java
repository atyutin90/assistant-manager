package ru.otus.dto;

import lombok.Builder;

@Builder
public record ProjectQuestionLevelDto(
    Long questionId,

    Boolean enabled,

    String uuid,

    String projectRole,

    String skill,

    String areaKnowledge,

    String section,

    String text,

    Long careerLevelId
){ }
