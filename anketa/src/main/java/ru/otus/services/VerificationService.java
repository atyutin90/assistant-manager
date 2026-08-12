package ru.otus.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.VerificationDetailsDto;
import ru.otus.dto.VerificationFormDto;
import ru.otus.dto.VerificationItemDto;

public interface VerificationService {

    Page<VerificationItemDto> findPending(Long userId, Pageable pageable);

    VerificationDetailsDto findDetails(Long staffEvaluationUserId, Long verifierId);

    void save(Long staffEvaluationUserId, Long verifierId, VerificationFormDto form);

    void confirmAll(Long staffEvaluationUserId, Long verifierId);

    void complete(Long staffEvaluationUserId, Long verifierId);
}
