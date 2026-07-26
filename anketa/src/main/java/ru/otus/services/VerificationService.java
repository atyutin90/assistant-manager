package ru.otus.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.VerificationDetailsDto;
import ru.otus.dto.VerificationFormDto;
import ru.otus.dto.VerificationItemDto;

public interface VerificationService {

    Page<VerificationItemDto> findPending(Long userId, Pageable pageable);

    String findStartQuestion(Long userId, Long verifierId);

    VerificationDetailsDto findDetails(Long userId, Long verifierId, String questionUuid);

    void save(Long userId, Long verifierId, VerificationFormDto form, boolean finish);
}
