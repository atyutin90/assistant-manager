package ru.otus.config.restclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.otus.config.ai.LiteLlmConfig;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Configuration
public class LiteLlmRestClientConfig {

    @Bean(name = "liteLlmRestClient")
    public RestClient liteLlmRestClient(LiteLlmConfig config) {
        return RestClient.builder().baseUrl(config.getBaseUrl())
            .defaultHeader(AUTHORIZATION, "Bearer %s".formatted(config.getApiKey()))
            .build();
    }
}
