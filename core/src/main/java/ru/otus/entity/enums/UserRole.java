package ru.otus.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.otus.annotations.ValueList;

import java.util.Arrays;
import java.util.Optional;

@Getter
@ValueList(type = "user-role")
@RequiredArgsConstructor
public enum UserRole implements OrderedEnum {
    ADMIN(4),
    MANAGER(3),
    TEAM_LEAD(2),
    USER(1);

    private final int order;

    public static Optional<UserRole> userRoleOf(String value) {
        return value == null ? Optional.empty() : Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(value)).findFirst();
    }
}
