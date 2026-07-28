package ru.otus.dto;

import lombok.Builder;

@Builder
public record ManagerAccessDto(
    Long id,
    String name,
    String username,
    boolean selected
) {
}
