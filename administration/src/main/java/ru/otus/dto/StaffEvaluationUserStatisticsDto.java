package ru.otus.dto;

import lombok.Builder;
import ru.otus.entity.enums.StaffEvaluationUserStatus;

@Builder
public record StaffEvaluationUserStatisticsDto(
    Long id,
    String lastName,
    String middleName,
    String firstName,
    String username,
    StaffEvaluationUserStatus status
) {
}
