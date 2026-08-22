package ru.otus.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DataNotFoundException extends RuntimeException {
    private final String message;
}
