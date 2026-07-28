package ru.otus.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class WebApplicationException extends RuntimeException {
    private final HttpStatus status;

    private final String message;

    public static WebApplicationException errorOf(HttpStatus status, String message) {
        return new WebApplicationException(status, message);
    }
}
