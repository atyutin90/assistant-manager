package ru.otus.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AiEmployeeMatch(
    Long id,
    Long projectRoleId,
    List<String> confirmedAnswers
) {
}
