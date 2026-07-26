package ru.otus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.otus.entity.enums.AnswerResponse;

@Builder
public record SurveyAnswerDto(
    @NotNull
    AnswerResponse response
) { }
