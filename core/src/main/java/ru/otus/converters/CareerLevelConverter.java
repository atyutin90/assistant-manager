package ru.otus.converters;

import ru.otus.dto.CareerLevelDto;
import ru.otus.entity.CareerLevel;

public class CareerLevelConverter {

    public static CareerLevelDto dtoOf(CareerLevel data) {
        return CareerLevelDto.builder()
            .id(data.getId())
            .code(data.getCode())
            .enabled(data.getEnabled())
            .name(data.getName())
            .position(data.getPosition())
            .build();
    }

    public static CareerLevel of(CareerLevelDto data) {
        return CareerLevel.builder()
            .id(data.id())
            .code(data.code())
            .enabled(data.enabled())
            .name(data.name())
            .position(data.position())
            .build();
    }
}
