package ru.otus.dto;

import lombok.Builder;

@Builder
public record AiChatResponse(String answer, long duration) {
}
