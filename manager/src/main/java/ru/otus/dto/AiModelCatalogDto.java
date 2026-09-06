package ru.otus.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AiModelCatalogDto(
    List<AiProviderOptionDto> providers,
    List<AiModelOptionDto> models
) {
    @Builder
    public record AiProviderOptionDto(String id, String displayName) {
    }

    @Builder
    public record AiModelOptionDto(String id, String provider, String displayName) {
    }

    public boolean contains(String provider, String model) {
        return models.stream()
            .anyMatch(option -> option.provider().equals(provider) && option.id().equals(model));
    }
}
