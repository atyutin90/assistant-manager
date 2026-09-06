package ru.otus.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AiModelSearchResult(String answer, List<AiModelEmployeeSelection> employees) {

    @Builder
    public record AiModelEmployeeSelection(Long id, String description) {
    }
}
