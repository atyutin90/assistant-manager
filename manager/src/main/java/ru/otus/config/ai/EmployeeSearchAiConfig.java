package ru.otus.config.ai;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmployeeSearchAiConfig {

    @Bean
    public OpenAiChatModel employeeSearchChatModel(
        LiteLlmConfig liteLlmConfig,
        ObservationRegistry observationRegistry
    ) {
        return OpenAiChatModel.builder()
            .options(OpenAiChatOptions.builder()
                .baseUrl(liteLlmConfig.getBaseUrl())
                .apiKey(liteLlmConfig.getApiKey())
                .build())
            .observationRegistry(observationRegistry)
            .build();
    }

    @Bean
    public ChatClient employeeSearchChatClient(
        OpenAiChatModel employeeSearchChatModel,
        ObservationRegistry observationRegistry
    ) {
        return ChatClient.builder(employeeSearchChatModel, observationRegistry, null, null)
            .build();
    }
}
