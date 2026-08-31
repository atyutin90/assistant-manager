package ru.otus.dto;

import lombok.Builder;
import java.util.Set;

@Builder
public record CurrentUser(
    Long id,

    String lastName,

    String middleName,

    String firstName,

    String username,

    String email,

    String password,

    Set<Long> projectRoles,

    //Текущий КУ
    Long currentLevel,

    //Должность по ТК
    String laborCodePosition,

    Long responsibleId,

    Set<String> userRoles
) {
}
