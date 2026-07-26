package ru.otus.entity.converters;

import ru.otus.dto.UserDto;
import ru.otus.entity.User;
import ru.otus.entity.enums.UserRole;

import static java.util.stream.Collectors.toSet;
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
            .projectRole(data.getProjectRole() != null ? data.getProjectRole().getId() : null)
            .currentLevel(data.getCurrentLevel() != null ? data.getCurrentLevel().getId() : null)
            .laborCodePosition(data.getLaborCodePosition())
            .responsibleId(data.getResponsible() != null ? data.getResponsible().getId() : null)
            .userRoles(data.getRoles().stream().map(UserRole::name).collect(toSet()))
            .build();
    }

    public static String displayNameOf(User user) {
        return "[" + (user.getProjectRole() != null ? user.getProjectRole().getName() : EMPTY) + "]" +
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
