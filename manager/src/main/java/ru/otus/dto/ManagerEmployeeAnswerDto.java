package ru.otus.dto;

import lombok.Builder;
import ru.otus.entity.enums.AnswerResponse;

@Builder
public record ManagerEmployeeAnswerDto(
    String areaKnowledge,
    String section,
    String question,
    AnswerResponse response,
    AnswerResponse verifiedResponse,
    String verificationComment
) {
}
