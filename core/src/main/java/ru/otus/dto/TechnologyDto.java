package ru.otus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TechnologyDto(
    Long id,

    @NotNull
    Boolean enabled,

    @NotBlank
    String name
) {}
