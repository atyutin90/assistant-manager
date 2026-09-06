package ru.otus.services.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.dto.AiModelCatalogDto;
import ru.otus.services.client.litellm.LiteLlmClient;
import ru.otus.services.client.litellm.dto.AiModelCatalog;

@Service
@RequiredArgsConstructor
public class LiteLlmModelCatalogServiceImpl implements LiteLlmModelCatalogService {

    private final LiteLlmClient liteLlmClient;

    @Override
    public AiModelCatalogDto getCatalog() {
        return dtoOf(liteLlmClient.getCatalogs());
    }

    private static AiModelCatalogDto dtoOf(AiModelCatalog catalog) {
        return AiModelCatalogDto.builder()
            .providers(catalog.providers().stream()
                .map(it -> AiModelCatalogDto.AiProviderOptionDto.builder()
                    .id(it.id())
                    .displayName(it.displayName())
                    .build())
                .toList()
            )
            .models(catalog.models().stream().map(it -> AiModelCatalogDto.AiModelOptionDto.builder()
                    .id(it.id())
                    .provider(it.provider())
                    .displayName(it.displayName())
                    .build())
                .toList()
            )
            .build();
    }
}
