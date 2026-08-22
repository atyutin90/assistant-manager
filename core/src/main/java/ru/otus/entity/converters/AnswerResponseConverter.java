package ru.otus.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.otus.entity.enums.AnswerResponse;

import static ru.otus.entity.enums.AnswerResponse.responseOf;

@Converter(autoApply = true)
public class AnswerResponseConverter implements AttributeConverter<AnswerResponse, String> {

    @Override
    public String convertToDatabaseColumn(AnswerResponse response) {
        return response != null ? response.name() : null;
    }

    @Override
    public AnswerResponse convertToEntityAttribute(String value) {
        return responseOf(value).orElse(null);
    }
}
