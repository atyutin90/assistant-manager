package ru.otus.converters;

import ru.otus.dto.TechnologyDto;
import ru.otus.entity.Technology;

public class TechnologyConverter {

    public static TechnologyDto dtoOf(Technology data) {
        return TechnologyDto.builder()
            .id(data.getId())
            .enabled(data.getEnabled())
            .name(data.getName())
            .build();
    }

    public static Technology of(TechnologyDto data) {
        return Technology.builder()
            .id(data.id())
            .enabled(data.enabled())
            .name(data.name())
            .build();
    }
}
