package ru.otus.dto;

import lombok.Builder;

@Builder
public record AiQuestionCandidate(Long id, String text) {
}
