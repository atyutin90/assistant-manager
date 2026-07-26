package ru.otus.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.otus.entity.enums.StaffEvaluationUserStatus;

import static ru.otus.entity.enums.StaffEvaluationUserStatus.staffEvaluationUserStatusOf;

@Converter(autoApply = true)
public class StaffEvaluationUserStatusConverter implements AttributeConverter<StaffEvaluationUserStatus, String> {

    @Override
    public String convertToDatabaseColumn(StaffEvaluationUserStatus status) {
        return status != null ? status.name() : null;
    }

    @Override
    public StaffEvaluationUserStatus convertToEntityAttribute(String value) {
        return staffEvaluationUserStatusOf(value).orElse(null);
    }
}
