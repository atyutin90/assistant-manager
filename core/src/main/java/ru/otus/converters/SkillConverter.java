package ru.otus.converters;

import ru.otus.dto.SkillDto;
import ru.otus.entity.Skill;

public class SkillConverter {

    public static SkillDto dtoOf(Skill data) {
        return SkillDto.builder()
            .id(data.getId())
            .code(data.getCode())
            .enabled(data.getEnabled())
            .name(data.getName())
            .position(data.getPosition())
            .build();
    }

    public static Skill of(SkillDto data) {
        return Skill.builder()
            .id(data.id())
            .code(data.code())
            .enabled(data.enabled())
            .name(data.name())
            .position(data.position())
            .build();
    }
}
