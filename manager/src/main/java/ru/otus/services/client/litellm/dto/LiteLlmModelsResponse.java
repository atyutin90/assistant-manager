package ru.otus.services.client.litellm.dto;

import java.util.List;

public record LiteLlmModelsResponse(List<LiteLlmModel> data) {

    public record LiteLlmModel(String id) {
    }
}
