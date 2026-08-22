package ru.otus.dto;

import lombok.Builder;
import ru.otus.entity.enums.TechnologyLevel;

@Builder
public record UserTechnologyDto(
    Long id,
    Long technologyId,
    String technologyName,
    TechnologyLevel level
) { }
