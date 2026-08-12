package ru.otus.converters;

import ru.otus.dto.ProjectDto;
import ru.otus.dto.ProjectManagerDto;
import ru.otus.entity.AssessmentProject;
import ru.otus.entity.AssessmentProjectAccess;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class ProjectDtoConverter {

    public static ProjectDto dtoOf(AssessmentProject project) {
        return ProjectDto.builder()
            .id(project.getId())
            .name(project.getName())
            .description(project.getDescription())
            .active(project.getActive())
            .owner(ownerOf(project))
            .editAccessManagers(editAccessManagersOf(project))
            .readAccessManagers(readAccessManagersOf(project))
            .build();
    }

    private static ProjectManagerDto ownerOf(AssessmentProject project) {
        var owner = project.getOwner();
        return owner != null ? ProjectManagerDto.builder()
            .id(owner.getId())
            .username(owner.getUsername())
            .build() : null;
    }

    private static Set<ProjectManagerDto> readAccessManagersOf(AssessmentProject project) {
        return project.getAccesses().stream()
            .filter(AssessmentProjectAccess::getReadAccess)
            .map(a -> ProjectManagerDto.builder()
                .id(a.getManager().getId())
                .username(a.getManager().getUsername())
                .build()
            ).collect(toSet());
    }

    private static Set<ProjectManagerDto> editAccessManagersOf(AssessmentProject project) {
        return project.getAccesses().stream()
            .filter(AssessmentProjectAccess::getEditAccess)
            .map(a -> ProjectManagerDto.builder()
                .id(a.getManager().getId())
                .username(a.getManager().getUsername())
                .build()
            ).collect(toSet());
    }
}
