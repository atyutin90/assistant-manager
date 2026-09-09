package ru.otus.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AiChatHistoryItem(
    String message,
    String answer,
    long duration,
    Instant createdAt
) {
}
