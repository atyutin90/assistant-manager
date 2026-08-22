package ru.otus.messaging;

public record EmailMessage(
    String to,
    String title,
    String text
) {
}
