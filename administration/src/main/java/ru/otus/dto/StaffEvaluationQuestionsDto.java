package ru.otus.dto;

import lombok.Builder;
import ru.otus.entity.enums.StaffEvaluationStatus;

import java.util.Set;

import static ru.otus.entity.enums.StaffEvaluationStatus.COMPLETED;
import static ru.otus.entity.enums.StaffEvaluationStatus.DRAFT;

@Builder
public record StaffEvaluationQuestionsDto(
    Long id,
    String name,
    StaffEvaluationStatus status,
    Set<Long> questionIds
) {
    public boolean canAdd() {
        return !COMPLETED.equals(status);
    }

    public boolean canRemove() {
        return DRAFT.equals(status);
    }

    public boolean canModifyPositions() {
        return !COMPLETED.equals(status);
    }
}
