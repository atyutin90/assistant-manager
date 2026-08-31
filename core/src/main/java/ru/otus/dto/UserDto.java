package ru.otus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.With;

import java.util.Set;

@With
@Builder
public record UserDto(
    Long id,

    @NotBlank
    String lastName,

    @NotBlank
    String middleName,

    @NotBlank
    String firstName,

    @NotBlank
    String username,

    @Email
    @NotBlank String email,

    String password,

    Set<Long> projectRoles,

    //Текущий КУ
    Long currentLevel,

    //Должность по ТК
    String laborCodePosition,

    Long responsibleId,

    @NotEmpty
    Set<String> userRoles
) {
}
