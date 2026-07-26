package ru.otus.dto;

import lombok.Builder;

@Builder
public record ProfileDto(
    Long id,
    String lastName,
    String middleName,
    String firstName,
    String username,
    String projectRole,
    //Текущий КУ
    String currentLevel,
    //Должность по ТК
    String laborCodePosition
) {
}
