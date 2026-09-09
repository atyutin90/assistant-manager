package ru.otus.services.ai;

import ru.otus.dto.AiChatHistoryItem;

import java.util.List;

public interface EmployeeChatHistoryService {

    List<AiChatHistoryItem> findHistory(Long managerId);

    void save(Long managerId, String message, String answer, long duration);

    void clearHistory(Long managerId);
}
