package ru.otus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

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

    ProjectManagerDto owner,

    Set<ProjectManagerDto> editAccessManagers,

    Set<ProjectManagerDto> readAccessManagers
) {
}
