package ru.otus.converters;

import ru.otus.dto.StaffEvaluationUserStatisticsDto;
import ru.otus.entity.StaffEvaluationUser;

public class StaffEvaluationUserStatisticsDtoConverter {

    public static StaffEvaluationUserStatisticsDto dtoOf(StaffEvaluationUser data) {
        var user = data.getUser();
        return StaffEvaluationUserStatisticsDto.builder()
            .id(data.getId())
            .lastName(user != null ? user.getLastName() : null)
            .middleName(user != null ? user.getMiddleName() : null)
            .firstName(user != null ? user.getFirstName() : null)
            .username(user != null ? user.getUsername() : null)
            .projectRoleId(data.getProjectRole() != null ? data.getProjectRole().getId() : null)
            .status(data.getStatus())
            .build();
    }
}
