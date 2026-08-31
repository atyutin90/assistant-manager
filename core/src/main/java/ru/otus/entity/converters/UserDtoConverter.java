package ru.otus.entity.converters;

import ru.otus.dto.UserDto;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.User;
import ru.otus.entity.enums.UserRole;

import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

public class UserDtoConverter {

    public static UserDto dtoOf(User data) {
        return UserDto.builder()
            .id(data.getId())
            .lastName(data.getLastName())
            .middleName(data.getMiddleName())
            .firstName(data.getFirstName())
            .username(data.getUsername())
            .email(data.getEmail())
            .password(null)
            .projectRoles(projectRolesOf(data))
            .currentLevel(data.getCurrentLevel() != null ? data.getCurrentLevel().getId() : null)
            .laborCodePosition(data.getLaborCodePosition())
            .responsibleId(data.getResponsible() != null ? data.getResponsible().getId() : null)
            .userRoles(userRolesOf(data))
            .build();
    }

    private static Set<String> userRolesOf(User data) {
        return isNotEmpty(data.getRoles()) ?
            data.getRoles().stream().map(UserRole::name).collect(toSet()) :
            Set.of();
    }

    private static Set<Long> projectRolesOf(User data) {
        return isNotEmpty(data.getProjectRoles()) ?
            data.getProjectRoles().stream().map(ProjectRole::getId).collect(toSet()) :
            Set.of();
    }

    public static String displayNameOf(User user) {
        return "[" +
            (isNotEmpty(user.getProjectRoles()) ?
                user.getProjectRoles().stream()
                    .map(ProjectRole::getName)
                    .collect(joining(", ")) :
                EMPTY
            ) + "]" +
            SPACE +
            user.getLastName() +
            SPACE +
            user.getFirstName() +
            SPACE +
            user.getMiddleName() +
            SPACE +
            "(" +
            user.getUsername() +
            ")";
    }
}
