package ru.otus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Builder
public record AiManagerSettingForm(
    @NotBlank
    @Size(max = 40)
    String provider,

    @NotBlank
    @Size(max = 255)
    String model,

    @Size(max = 2048)
    String apiKey
) {
    public boolean isApiKeyConfigured() {
        return isNotEmpty(apiKey);
    }
}
