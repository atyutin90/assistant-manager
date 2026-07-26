package ru.otus.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.AssignedStaffEvaluationDto;
import ru.otus.dto.StaffEvaluationResultDto;

import java.util.List;

public interface StaffEvaluationUserService {

    Page<AssignedStaffEvaluationDto> findAssigned(Long userId, Pageable pageable);

    List<AssignedStaffEvaluationDto> findActive(Long userId);

    StaffEvaluationResultDto findResult(Long userId, Long staffEvaluationId);
}
