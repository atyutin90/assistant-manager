package ru.otus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.With;

@With
@Builder
public record EmployeeDto(
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

    Long projectRole,

    //Текущий КУ
    Long currentLevel,

    //Должность по ТК
    String laborCodePosition,

    String responsible
) {
}
