package ru.otus.services.client.litellm.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AiModelCatalog(
    List<AiProviderOption> providers,
    List<AiModelOption> models
) {
    @Builder
    public record AiProviderOption(String id, String displayName) {
    }

    @Builder
    public record AiModelOption(String id, String provider, String displayName) {
    }
}