package ru.otus.dto;

import lombok.Builder;
import ru.otus.entity.enums.StaffEvaluationStatus;
import ru.otus.entity.enums.StaffEvaluationUserStatus;

import java.time.LocalDate;

@Builder
public record AssignedStaffEvaluationDto(
    Long staffEvaluationUserId,
    Long staffEvaluationId,
    String name,
    LocalDate dateFrom,
    LocalDate dateTo,
    StaffEvaluationStatus staffEvaluationStatus,
    String projectRole,
    StaffEvaluationUserStatus staffEvaluationUserStatus
) {
}
