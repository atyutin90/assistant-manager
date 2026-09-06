package ru.otus.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.AiChatRequest;
import ru.otus.dto.AiChatResponse;
import ru.otus.dto.CurrentUser;
import ru.otus.services.ai.EmployeeChatService;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequiredArgsConstructor
public class ManagerEmployeeSearchRestController {

    private final EmployeeChatService employeeChatService;

    private final MessageSource messageSource;

    @PostMapping("/api/employees/ai-search")
    public ResponseEntity<AiChatResponse> search(
        @Valid @RequestBody AiChatRequest request,
        @CurrentUserParam CurrentUser currentUser
    ) {
        var startedAt = nanoTime();
        try {
            return ok(AiChatResponse.builder()
                .answer(employeeChatService.ask(currentUser.id(), request.message()))
                .duration(elapsedMillis(startedAt, nanoTime()))
                .build()
            );
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

    private static long elapsedMillis(long startedAt, long endAt) {
        return NANOSECONDS.toMillis(endAt - startedAt);
    }
}
