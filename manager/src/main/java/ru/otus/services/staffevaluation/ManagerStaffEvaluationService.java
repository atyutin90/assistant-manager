package ru.otus.services.staffevaluation;

import ru.otus.dto.ManagerStaffEvaluationDto;

import java.util.List;

public interface ManagerStaffEvaluationService {

    List<ManagerStaffEvaluationDto> findAll();

    List<ManagerStaffEvaluationDto> findAllByEmployeeIdAndProjectRoleId(Long employeeId, Long projectRoleId);
}
