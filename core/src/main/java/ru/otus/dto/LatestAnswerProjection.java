package ru.otus.dto;

import lombok.Builder;

@Builder
public record LatestAnswerProjection(
    Long userId,
    Long questionId,
    Long projectRoleId,
    String questionText
) {
}
