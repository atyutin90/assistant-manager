package ru.otus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.With;
import org.springframework.format.annotation.DateTimeFormat;
import ru.otus.annotations.LessThanField;
import ru.otus.entity.enums.StaffEvaluationStatus;

import java.time.LocalDate;

import static ru.otus.entity.enums.StaffEvaluationStatus.ACTIVE;
import static ru.otus.entity.enums.StaffEvaluationStatus.COMPLETED;
import static ru.otus.entity.enums.StaffEvaluationStatus.DRAFT;

@With
@Builder
@LessThanField(dateFrom = "dateFrom", dateTo = "dateTo")
public record StaffEvaluationDto(
    Long id,

    @NotBlank
    String name,

    @NotNull(message = "{jakarta.validation.constraints.NotBlank.message}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate dateFrom,

    @NotNull(message = "{jakarta.validation.constraints.NotBlank.message}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate dateTo,

    @NotNull
    StaffEvaluationStatus status
) {
    public boolean isDraft() {
        return DRAFT.equals(status);
    }

    public boolean isActive() {
        return ACTIVE.equals(status);
    }

    public boolean isCompleted() {
        return COMPLETED.equals(status);
    }
}
