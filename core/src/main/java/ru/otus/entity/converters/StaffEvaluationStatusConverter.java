package ru.otus.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.otus.entity.enums.StaffEvaluationStatus;

import static ru.otus.entity.enums.StaffEvaluationStatus.staffEvaluationStatusOf;

@Converter(autoApply = true)
public class StaffEvaluationStatusConverter implements AttributeConverter<StaffEvaluationStatus, String> {

    @Override
    public String convertToDatabaseColumn(StaffEvaluationStatus response) {
        return response != null ? response.name() : null;
    }

    @Override
    public StaffEvaluationStatus convertToEntityAttribute(String value) {
        return staffEvaluationStatusOf(value).orElse(null);
    }
}
