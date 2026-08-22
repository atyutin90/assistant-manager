package ru.otus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ru.otus.annotations.MismatchField;

@Builder
@MismatchField(firstField = "newPassword", secondField = "passwordConfirmation")
public record UserPasswordDto(
    @NotBlank
    @Size(min = 4, max = 10)
    String newPassword,

    @NotBlank
    @Size(min = 4, max = 10)
    String passwordConfirmation
) {
}
