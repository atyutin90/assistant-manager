package ru.otus.services.employee;

import ru.otus.dto.ManagerEmployeeDto;
import ru.otus.dto.ManagerEmployeeDetailsDto;
import ru.otus.dto.filter.ManagerUserFilter;

import java.util.List;

public interface ManagerEmployeeService {

    List<ManagerEmployeeDto> findAll(ManagerUserFilter filter);

    ManagerEmployeeDetailsDto findDetails(Long employeeId, Long staffEvaluationId, Long projectId, Long managerId);
}
