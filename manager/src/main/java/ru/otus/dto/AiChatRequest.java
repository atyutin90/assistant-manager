package ru.otus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AiChatRequest(
    @NotBlank
    @Size(max = 2000)
    String message
) {
}
