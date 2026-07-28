package ru.otus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProjectDto(
    Long id,

    @NotBlank
    @Size(max = 255)
    String name,

    @Size(max = 1000)
    String description,

    @NotNull
    Boolean active,

    Long ownerId,

    String ownerUsername
) {
}
