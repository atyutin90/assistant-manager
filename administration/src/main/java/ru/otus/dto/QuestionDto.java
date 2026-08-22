package ru.otus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.With;

@With
@Builder
public record QuestionDto(
    Long id,

    @NotNull
    Boolean enabled,

    @NotBlank
    String uuid,

    @NotNull(message = "{jakarta.validation.constraints.NotEmpty.message}")
    Long projectRole,

    @NotNull(message = "{jakarta.validation.constraints.NotEmpty.message}")
    Long skill,

    @NotBlank
    String areaKnowledge,

    String section,

    @NotBlank
    String text
) {
}
