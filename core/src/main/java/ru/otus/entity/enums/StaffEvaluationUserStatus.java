package ru.otus.entity.enums;

import java.util.Arrays;
import java.util.Optional;

public enum StaffEvaluationUserStatus {
    NEW,
    IN_PROGRESS,
    FEEDBACK,
    VERIFICATION,
    COMPLETED;

    public static Optional<StaffEvaluationUserStatus> staffEvaluationUserStatusOf(String value) {
        return value == null ? Optional.empty() : Arrays.stream(values())
            .filter(status -> status.name().equalsIgnoreCase(value))
            .findFirst();
    }
}
