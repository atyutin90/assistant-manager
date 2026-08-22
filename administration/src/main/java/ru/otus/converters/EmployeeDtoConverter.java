package ru.otus.converters;

import ru.otus.dto.EmployeeDto;
import ru.otus.entity.User;
import ru.otus.entity.StaffEvaluationUser;

public class EmployeeDtoConverter {

    public static EmployeeDto dtoOf(StaffEvaluationUser data) {
        EmployeeDto result = null;
        User user = data.getUser();
        if (user != null) {
            result = EmployeeDto.builder()
                .id(user.getId())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .firstName(user.getFirstName())
                .username(user.getUsername())
                .email(user.getEmail())
                .projectRole(user.getProjectRole() != null ? user.getProjectRole().getId() : null)
                .currentLevel(user.getCurrentLevel() != null ? user.getCurrentLevel().getId() : null)
                .laborCodePosition(user.getLaborCodePosition())
                .responsible(user.getResponsible() != null ? user.getResponsible().getDisplayName() : null)
                .build();
        }
        return result;
    }
}
