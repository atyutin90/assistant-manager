package ru.otus.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum AnswerResponse implements OrderedEnum {
    YES(1),
    NO(2),
    UNKNOWN(3);

    private final int order;

    public static Optional<AnswerResponse> responseOf(String value) {
        return value == null ? Optional.empty() : Arrays.stream(values())
            .filter(it -> it.name().equalsIgnoreCase(value)).findFirst();
    }
}
