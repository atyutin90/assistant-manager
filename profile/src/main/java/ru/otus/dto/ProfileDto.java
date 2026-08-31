package ru.otus.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record ProfileDto(
    Long id,
    String lastName,
    String middleName,
    String firstName,
    String username,
    Set<String> projectRoles,
    //Текущий КУ
    String currentLevel,
    //Должность по ТК
    String laborCodePosition
) {
}
