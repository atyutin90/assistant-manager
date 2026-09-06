package ru.otus.config.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.ai.litellm")
public class LiteLlmConfig {
    private String baseUrl;

    private String apiKey;
}
