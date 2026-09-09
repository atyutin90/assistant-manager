package ru.otus.services.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.dto.AiChatHistoryItem;
import ru.otus.entity.EmployeeSearchHistory;
import ru.otus.repositories.EmployeeAiSearchHistoryRepository;
import ru.otus.repositories.UserRepository;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeChatHistoryServiceImpl implements EmployeeChatHistoryService {

    static final int HISTORY_SIZE = 10;

    private final EmployeeAiSearchHistoryRepository historyRepository;

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AiChatHistoryItem> findHistory(Long managerId) {
        var history = historyRepository.findByManagerIdOrderByCreatedAtDescIdDesc(
            managerId,
            PageRequest.of(0, HISTORY_SIZE)
        );
        return history.reversed().stream()
            .map(item -> AiChatHistoryItem.builder()
                .message(item.getMessage())
                .answer(item.getAnswer())
                .duration(item.getDuration())
                .createdAt(item.getCreatedAt())
                .build())
            .toList();
    }

    @Override
    @Transactional
    public void save(Long managerId, String message, String answer, long duration) {
        historyRepository.save(EmployeeSearchHistory.builder()
            .manager(userRepository.getReferenceById(managerId))
            .message(message.trim())
            .answer(answer)
            .duration(duration)
            .createdAt(Instant.now())
            .build());
        historyRepository.deleteOlderThanLimit(managerId, HISTORY_SIZE);
    }

    @Override
    @Transactional
    public void clearHistory(Long managerId) {
        historyRepository.deleteByManagerId(managerId);
    }
}
