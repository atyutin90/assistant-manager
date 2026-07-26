package ru.otus.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class NonUniqueValueException extends RuntimeException {
    private final Map<String, String> info;
}
