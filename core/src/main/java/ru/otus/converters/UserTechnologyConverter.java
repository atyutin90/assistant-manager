package ru.otus.converters;

import ru.otus.dto.UserTechnologyDto;
import ru.otus.entity.UserTechnology;

public class UserTechnologyConverter {

    public static UserTechnologyDto dtoOf(UserTechnology data) {
        return UserTechnologyDto.builder()
            .id(data.getId())
            .technologyId(data.getTechnology().getId())
            .technologyName(data.getTechnology().getName())
            .level(data.getLevel())
            .build();
    }
}
