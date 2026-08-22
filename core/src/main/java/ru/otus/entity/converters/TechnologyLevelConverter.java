package ru.otus.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.otus.entity.enums.TechnologyLevel;

import static ru.otus.entity.enums.TechnologyLevel.technologyLevelOf;

@Converter(autoApply = true)
public class TechnologyLevelConverter implements AttributeConverter<TechnologyLevel, String> {

    @Override
    public String convertToDatabaseColumn(TechnologyLevel level) {
        return level != null ? level.name() : null;
    }

    @Override
    public TechnologyLevel convertToEntityAttribute(String value) {
        return technologyLevelOf(value).orElse(null);
    }
}
