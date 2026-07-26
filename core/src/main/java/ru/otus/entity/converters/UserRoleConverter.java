package ru.otus.entity.converters;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.otus.entity.enums.UserRole;

import static ru.otus.entity.enums.UserRole.userRoleOf;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole userRole) {
        return userRole != null ? userRole.name() : null;
    }

    @Override
    public UserRole convertToEntityAttribute(String value) {
        return userRoleOf(value).orElse(null);
    }
}
