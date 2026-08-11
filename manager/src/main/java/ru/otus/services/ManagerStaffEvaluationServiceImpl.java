package ru.otus.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.otus.dto.ManagerStaffEvaluationDto;
import ru.otus.entity.StaffEvaluation;
import ru.otus.entity.enums.StaffEvaluationStatus;
import ru.otus.repositories.StaffEvaluationRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static ru.otus.entity.enums.StaffEvaluationStatus.COMPLETED;

@Service
@RequiredArgsConstructor
public class ManagerStaffEvaluationServiceImpl implements ManagerStaffEvaluationService {

    private final StaffEvaluationRepository staffEvaluationRepository;

    @Override
    public List<ManagerStaffEvaluationDto> findAll() {
        return staffEvaluationRepository.findAllByStatusOrderByDateFromDesc(COMPLETED, Limit.of(100)).stream()
            .map(this::dtoOf)
            .toList();
    }

    private ManagerStaffEvaluationDto dtoOf(StaffEvaluation staffEvaluation) {
        return ManagerStaffEvaluationDto.builder()
            .id(staffEvaluation.getId())
            .name(staffEvaluation.getName())
            .dateFrom(staffEvaluation.getDateFrom())
            .build();
    }
}
