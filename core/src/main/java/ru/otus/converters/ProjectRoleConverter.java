package ru.otus.converters;

import ru.otus.dto.ProjectRoleDto;
import ru.otus.entity.ProjectRole;

public class ProjectRoleConverter {

    public static ProjectRoleDto dtoOf(ProjectRole data) {
        return ProjectRoleDto.builder()
            .id(data.getId())
            .code(data.getCode())
            .enabled(data.getEnabled())
            .name(data.getName())
            .position(data.getPosition())
            .build();
    }

    public static ProjectRole of(ProjectRoleDto data) {
        return ProjectRole.builder()
            .id(data.id())
            .code(data.code())
            .enabled(data.enabled())
            .name(data.name())
            .position(data.position())
            .build();
    }
}
