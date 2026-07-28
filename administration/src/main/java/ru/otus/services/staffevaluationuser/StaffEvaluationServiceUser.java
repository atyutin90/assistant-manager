package ru.otus.services.staffevaluationuser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.EmployeeDto;
import ru.otus.dto.filter.StaffEvaluationUserFilter;

public interface StaffEvaluationServiceUser {

    Page<EmployeeDto> findAll(StaffEvaluationUserFilter filter, Pageable pageable);
}
