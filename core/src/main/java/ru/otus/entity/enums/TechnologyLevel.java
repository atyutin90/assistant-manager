package ru.otus.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.otus.annotations.ValueList;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Optional.empty;

@ValueList(type = "technology-level")
@Getter
@RequiredArgsConstructor
public enum TechnologyLevel implements OrderedEnum {
    BASIC_KNOWLEDGE(1),
    BEGINNER_PRACTICE(2),
    CONFIDENT(3),
    EXPERT(4);

    private final int order;

    public static Optional<TechnologyLevel> technologyLevelOf(String value) {
        return value == null ? empty() : Arrays.stream(values())
            .filter(status -> status.name().equalsIgnoreCase(value))
            .findFirst();
    }

}
