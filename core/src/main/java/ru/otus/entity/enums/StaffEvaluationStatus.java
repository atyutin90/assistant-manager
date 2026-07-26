package ru.otus.entity.enums;

import java.util.Arrays;
import java.util.Optional;

public enum StaffEvaluationStatus {
    DRAFT,
    ACTIVE,
    COMPLETED;

    public static Optional<StaffEvaluationStatus> staffEvaluationStatusOf(String value) {
        return value == null ? Optional.empty() : Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(value)).findFirst();
    }
}
