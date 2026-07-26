package ru.otus.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SurveyPageDto(
    Long staffEvaluationId,
    String evaluationName,
    String projectRole,
    List<SurveyQuestionDto> questions,
    SurveyQuestionDto currentQuestion,
    int currentNumber,
    boolean canFinish
) {
}
