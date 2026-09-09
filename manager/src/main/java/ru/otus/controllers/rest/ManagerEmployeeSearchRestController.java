package ru.otus.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.AiChatHistoryItem;
import ru.otus.dto.AiChatRequest;
import ru.otus.dto.AiChatResponse;
import ru.otus.dto.CurrentUser;
import ru.otus.services.ai.EmployeeChatService;
import ru.otus.services.ai.EmployeeChatHistoryService;
import ru.otus.services.ai.ManagerAiSettingService;

import java.util.List;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequiredArgsConstructor
public class ManagerEmployeeSearchRestController {

    private final EmployeeChatService employeeChatService;

    private final EmployeeChatHistoryService employeeChatHistoryService;

    private final ManagerAiSettingService managerAiSettingService;

    private final MessageSource messageSource;

    @GetMapping("/api/employees/search")
    public ResponseEntity<List<AiChatHistoryItem>> history(
        @CurrentUserParam CurrentUser currentUser
    ) {
        ResponseEntity<List<AiChatHistoryItem>> response;
        if (!managerAiSettingService.hasSetting(currentUser.username())) {
            response = status(FORBIDDEN).build();
        } else {
            response = ok(employeeChatHistoryService.findHistory(currentUser.id()));
        }
        return response;
    }

    @DeleteMapping("/api/employees/search")
    public ResponseEntity<Void> clearHistory(@CurrentUserParam CurrentUser currentUser) {
        employeeChatHistoryService.clearHistory(currentUser.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/employees/search")
    public ResponseEntity<AiChatResponse> search(
        @Valid @RequestBody AiChatRequest request,
        @CurrentUserParam CurrentUser currentUser
    ) {
        ResponseEntity<AiChatResponse> response;
        var startedAt = nanoTime();
        try {
            if (!managerAiSettingService.hasSetting(currentUser.username())) {
                response = status(FORBIDDEN).build();
            } else {
                var chatResponse = search(currentUser.id(), request.message(), startedAt);
                response = ok(chatResponse);
            }
            return response;
        } catch (Exception exception) {
            return status(SERVICE_UNAVAILABLE)
                .body(AiChatResponse.builder()
                    .answer(
                        messageSource.getMessage(
                            "error.ai-service-is-temporarily-unavailable",
                            new Object[]{},
                            getLocale()
                        ))
                    .duration(elapsedMillis(startedAt, nanoTime()))
                    .build()
                );
        }
    }

    private AiChatResponse search(Long managerId, String message, long startedAt) {
        var answer = employeeChatService.ask(managerId, message);
        var duration = elapsedMillis(startedAt, nanoTime());
        employeeChatHistoryService.save(managerId, message, answer, duration);
        return AiChatResponse.builder()
            .answer(answer)
            .duration(duration)
            .build();
    }

    private static long elapsedMillis(long startedAt, long endAt) {
        return NANOSECONDS.toMillis(endAt - startedAt);
    }
}
