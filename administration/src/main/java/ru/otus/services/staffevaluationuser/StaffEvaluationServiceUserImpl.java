package ru.otus.services.staffevaluationuser;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.otus.converters.EmployeeDtoConverter;
import ru.otus.converters.StaffEvaluationUserStatisticsDtoConverter;
import ru.otus.dto.EmployeeDto;
import ru.otus.dto.StaffEvaluationUserStatisticsDto;
import ru.otus.dto.filter.StaffEvaluationUserFilter;
import ru.otus.repositories.StaffEvaluationUserRepository;

import static ru.otus.dto.filter.specification.EmployeeSpecification.employeeFilterSpecification;

@Service
@RequiredArgsConstructor
public class StaffEvaluationServiceUserImpl implements StaffEvaluationServiceUser {

    private final StaffEvaluationUserRepository staffEvaluationUserRepository;

    @Override
    public Page<EmployeeDto> findAll(StaffEvaluationUserFilter filter, Pageable pageable) {
        var userSpecification = employeeFilterSpecification(filter);
        return staffEvaluationUserRepository.findAll(userSpecification, pageable)
            .map(EmployeeDtoConverter::dtoOf);
    }

    @Override
    public Page<StaffEvaluationUserStatisticsDto> findStatistics(StaffEvaluationUserFilter filter, Pageable pageable) {
        var userSpecification = employeeFilterSpecification(filter);
        return staffEvaluationUserRepository.findAll(userSpecification, pageable)
            .map(StaffEvaluationUserStatisticsDtoConverter::dtoOf);
    }
}
