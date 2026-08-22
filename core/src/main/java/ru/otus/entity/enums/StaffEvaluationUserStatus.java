package ru.otus.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.otus.annotations.ValueList;

import java.util.Arrays;
import java.util.Optional;

@ValueList(type = "staff-evaluation-user-status")
@Getter
@RequiredArgsConstructor
public enum StaffEvaluationUserStatus implements OrderedEnum {
    NEW(1),
    IN_PROGRESS(2),
    FEEDBACK(3),
    VERIFICATION(4),
    COMPLETED(5);

    private final int order;

    public static Optional<StaffEvaluationUserStatus> staffEvaluationUserStatusOf(String value) {
        return value == null ? Optional.empty() : Arrays.stream(values())
            .filter(status -> status.name().equalsIgnoreCase(value))
            .findFirst();
    }
}
