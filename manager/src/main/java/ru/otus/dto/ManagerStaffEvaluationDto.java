package ru.otus.dto;

import lombok.Builder;

@Builder
public record ManagerStaffEvaluationDto(
    Long id,
    String name
) {
}
