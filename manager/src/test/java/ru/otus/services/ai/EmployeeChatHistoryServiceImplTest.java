package ru.otus.services.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.otus.entity.EmployeeSearchHistory;
import ru.otus.entity.User;
import ru.otus.repositories.EmployeeAiSearchHistoryRepository;
import ru.otus.repositories.UserRepository;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Сервис истории AI-поиска сотрудников")
class EmployeeChatHistoryServiceImplTest {

    private static final long MANAGER_ID = 42L;

    @Mock
    private EmployeeAiSearchHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmployeeChatHistoryServiceImpl historyService;

    @Test
    @DisplayName("должен сохранять запрос и удалять записи сверх лимита")
    void shouldSaveRequestAndRemoveEntriesOverLimit() {
        var manager = User.builder().id(MANAGER_ID).build();
        when(userRepository.getReferenceById(MANAGER_ID)).thenReturn(manager);

        historyService.save(MANAGER_ID, "  Java developer  ", "Found", 150L);

        var order = inOrder(historyRepository);
        order.verify(historyRepository).save(any(EmployeeSearchHistory.class));
        order.verify(historyRepository).deleteOlderThanLimit(
            MANAGER_ID,
            EmployeeChatHistoryServiceImpl.HISTORY_SIZE
        );
    }

    @Test
    @DisplayName("должен возвращать не более десяти записей от старых к новым")
    void shouldReturnLimitedHistoryInChronologicalOrder() {
        var newer = historyItem(2L, "second", Instant.parse("2026-09-10T10:01:00Z"));
        var older = historyItem(1L, "first", Instant.parse("2026-09-10T10:00:00Z"));
        var page = PageRequest.of(0, EmployeeChatHistoryServiceImpl.HISTORY_SIZE);
        when(historyRepository.findByManagerIdOrderByCreatedAtDescIdDesc(MANAGER_ID, page))
            .thenReturn(List.of(newer, older));

        var result = historyService.findHistory(MANAGER_ID);

        assertThat(result).extracting("message").containsExactly("first", "second");
        verify(historyRepository).findByManagerIdOrderByCreatedAtDescIdDesc(MANAGER_ID, page);
    }

    @Test
    @DisplayName("должен удалять историю только указанного менеджера")
    void shouldClearManagerHistory() {
        historyService.clearHistory(MANAGER_ID);

        verify(historyRepository).deleteByManagerId(MANAGER_ID);
    }

    private static EmployeeSearchHistory historyItem(Long id, String message, Instant createdAt) {
        return EmployeeSearchHistory.builder()
            .id(id)
            .message(message)
            .answer("answer")
            .duration(100L)
            .createdAt(createdAt)
            .build();
    }
}
