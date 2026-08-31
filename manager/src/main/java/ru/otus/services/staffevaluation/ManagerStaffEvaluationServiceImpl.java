package ru.otus.services.staffevaluation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import ru.otus.dto.ManagerStaffEvaluationDto;
import ru.otus.entity.StaffEvaluation;
import ru.otus.entity.enums.StaffEvaluationUserStatus;
import ru.otus.repositories.StaffEvaluationRepository;

import java.util.List;
import java.util.Set;

import static ru.otus.entity.enums.StaffEvaluationStatus.ACTIVE;
import static ru.otus.entity.enums.StaffEvaluationStatus.COMPLETED;

@Service
@RequiredArgsConstructor
public class ManagerStaffEvaluationServiceImpl implements ManagerStaffEvaluationService {

    private static final int LIMIT_RECORDS = 50;

    private final StaffEvaluationRepository staffEvaluationRepository;

    @Override
    public List<ManagerStaffEvaluationDto> findAll() {
        return staffEvaluationRepository.findAllByStatusInOrderByDateFromDesc(
                Set.of(ACTIVE, COMPLETED),
                Limit.of(LIMIT_RECORDS)
            ).stream()
            .map(this::dtoOf)
            .toList();
    }

    @Override
    public List<ManagerStaffEvaluationDto> findAllByEmployeeIdAndProjectRoleId(Long employeeId, Long projectRoleId) {
        return staffEvaluationRepository.findAllByStatusInOrderByDateFromDesc(
                Set.of(ACTIVE, COMPLETED),
                Limit.of(LIMIT_RECORDS)).stream()
            .filter(se -> se.getStaffEvaluationUsers().stream()
                .anyMatch(seu -> employeeId.equals(seu.getUser().getId()) &&
                        seu.getProjectRole().getId().equals(projectRoleId) &&
                    Set.of(
                        StaffEvaluationUserStatus.COMPLETED,
                        StaffEvaluationUserStatus.FEEDBACK,
                        StaffEvaluationUserStatus.VERIFICATION
                    ).contains(seu.getStatus()))
            ).map(this::dtoOf)
            .toList();
    }

    private ManagerStaffEvaluationDto dtoOf(StaffEvaluation staffEvaluation) {
        return ManagerStaffEvaluationDto.builder()
            .id(staffEvaluation.getId())
            .name(staffEvaluation.displayName())
            .build();
    }
}
