package ru.otus.dto;

import lombok.Builder;
import ru.otus.entity.enums.AnswerResponse;

@Builder
public record SurveyAnswerCommand(
    Long staffEvaluationId,
    String projectRole,
    String questionUuid,
    Long userId,
    AnswerResponse response
) {
}
