package ru.otus.dto;

import lombok.Builder;
import ru.otus.entity.enums.AnswerResponse;

@Builder
public record VerificationQuestionDto(
    Long answerId,
    String uuid,
    Integer position,
    String areaKnowledge,
    String section,
    String question,
    AnswerResponse employeeResponse,
    AnswerResponse verifiedResponse,
    String comment
) {
}
