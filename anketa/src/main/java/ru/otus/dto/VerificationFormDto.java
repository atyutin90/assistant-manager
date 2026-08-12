package ru.otus.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ru.otus.annotations.NotEmptyDependentField;
import ru.otus.entity.enums.AnswerResponse;

@NotEmptyDependentField(
    mainField = "response",
    dependentField = "comment",
    expectedValues = {"NO"}
)
@Builder
public record VerificationFormDto(

    Long answerId,

    @NotNull
    AnswerResponse response,

    @Size(max = 4000)
    String comment
) {
}
