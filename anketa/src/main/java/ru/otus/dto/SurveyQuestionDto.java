package ru.otus.dto;

import lombok.Builder;
import ru.otus.entity.enums.AnswerResponse;

@Builder
public record SurveyQuestionDto(
    String uuid,
    String areaKnowledge,
    String section,
    String text,
    Integer position,
    AnswerResponse response
) {
}
