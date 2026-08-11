package ru.otus.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ManagerStaffEvaluationDto(
    Long id,
    String name,
    LocalDate dateFrom
) {
}
