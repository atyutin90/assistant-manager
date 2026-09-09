package ru.otus.services.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import ru.otus.dto.AiModelSearchResult;
import ru.otus.services.ai.tools.EmployeeSearchTools;

import java.util.Map;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.ai.chat.client.ChatClient.EntityParamSpec;
import static ru.otus.utils.FileUtils.resourceAsString;

@Service
@RequiredArgsConstructor
public class EmployeeChatServiceImpl implements EmployeeChatService {

    private static final String API_KEY = "api_key";

    private static final String SYSTEM_PROMPT = resourceAsString("/ai/SYSTEM_PROMT.md");

    private final ManagerAiSettingService managerAiSettingService;

    private final EmployeeSearchTools employeeSearchTools;

    private final EmployeeSearchService employeeSearchService;

    private final ChatClient employeeSearchChatClient;

    @Override
    public String ask(Long managerId, String message) {
        var credentials = managerAiSettingService.findSetting(managerId);
        var requestOptions = OpenAiChatOptions.builder()
            .model(credentials.model())
            .extraBody(
                isNotBlank(credentials.apiKey()) ?
                    Map.of(API_KEY, credentials.apiKey()) :
                    Map.of()
            );
        var modelResult = employeeSearchChatClient
            .prompt()
            .system(SYSTEM_PROMPT.formatted(projectRoleCatalog()))
            .user(message.trim())
            .options(requestOptions)
            .tools(employeeSearchTools)
            .call()
            .entity(AiModelSearchResult.class, EntityParamSpec::validateSchema);
        return employeeSearchService.resolveEmployeeData(modelResult);
    }

    private String projectRoleCatalog() {
        return employeeSearchService.findProjectRoles().stream()
            .map(role -> "%d | %s".formatted(role.id(), role.name()))
            .collect(joining(System.lineSeparator()));
    }
}
