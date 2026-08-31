package ru.otus.converters;

import ru.otus.dto.EmployeeDto;
import ru.otus.entity.User;
import ru.otus.entity.StaffEvaluationUser;

import java.util.Set;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class EmployeeDtoConverter {

    public static EmployeeDto dtoOf(StaffEvaluationUser data) {
        EmployeeDto result = null;
        User user = data.getUser();
        if (user != null) {
            result = EmployeeDto.builder()
                .id(data.getId())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .firstName(user.getFirstName())
                .username(user.getUsername())
                .email(user.getEmail())
                .projectRole(data.getProjectRole() != null ? data.getProjectRole().getId() : null)
                .currentLevel(user.getCurrentLevel() != null ? user.getCurrentLevel().getId() : null)
                .laborCodePosition(user.getLaborCodePosition())
                .responsible(responsibleNamesOf(data))
                .build();
        }
        return result;
    }

    private static String responsibleNamesOf(StaffEvaluationUser data) {
        String responsibleNames = null;
        Set<User> responsibles = data.getUser().getResponsibles();
        if (isNotEmpty(responsibles)) {
            responsibleNames = data.getUser().getResponsibles().stream()
                .map(User::getDisplayName)
                .sorted()
                .collect(joining(", "));
        }
        return responsibleNames;
    }
}
