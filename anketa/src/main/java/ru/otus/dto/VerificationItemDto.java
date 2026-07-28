package ru.otus.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record VerificationItemDto(
    Long staffEvaluationUserId,
    String name,
    LocalDate dateFrom,
    LocalDate dateTo,
    String employeeName,
    String employeeUsername
) {
}
