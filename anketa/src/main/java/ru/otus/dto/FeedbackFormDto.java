package ru.otus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record FeedbackFormDto(
    @NotBlank
    @Size(max = 4000)
    String message
) {
}
