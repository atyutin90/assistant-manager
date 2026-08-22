package ru.otus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProjectRoleDto(
    Long id,

    @NotNull
    Boolean enabled,

    @NotBlank
    String code,

    @NotBlank
    String name,

    @NotNull(message = "{jakarta.validation.constraints.NotBlank.message}")
    Integer position
) {}
