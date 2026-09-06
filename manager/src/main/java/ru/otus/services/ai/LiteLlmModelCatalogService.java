package ru.otus.services.ai;

import ru.otus.dto.AiModelCatalogDto;

public interface LiteLlmModelCatalogService {

    AiModelCatalogDto getCatalog();
}
