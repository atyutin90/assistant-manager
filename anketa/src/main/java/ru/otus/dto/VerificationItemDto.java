package ru.otus.dto;

import lombok.Builder;
import ru.otus.entity.enums.StaffEvaluationStatus;
import ru.otus.entity.enums.StaffEvaluationUserStatus;

import java.time.LocalDate;

@Builder
public record VerificationItemDto(
    Long staffEvaluationUserId,
    String name,
    LocalDate dateFrom,
    LocalDate dateTo,
    String employeeName,
    String employeeUsername,
    StaffEvaluationStatus staffEvaluationStatus,
    StaffEvaluationUserStatus staffEvaluationUserStatus
) {
}
