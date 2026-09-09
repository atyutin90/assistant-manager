package ru.otus.dto;

import lombok.Builder;

@Builder
public record AiEmployeeTechnology(
    String name,
    String level,
    int levelOrder
) {
}
