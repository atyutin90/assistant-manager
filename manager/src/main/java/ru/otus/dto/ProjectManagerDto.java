package ru.otus.dto;

import lombok.Builder;

@Builder
public record ProjectManagerDto(
    Long id,
    String username
) {
}
