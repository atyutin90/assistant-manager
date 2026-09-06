package ru.otus.services.client.litellm;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.otus.services.client.litellm.dto.AiModelCatalog;
import ru.otus.services.client.litellm.dto.LiteLlmModelsResponse;

import java.util.Comparator;
import java.util.List;

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.services.client.litellm.dto.AiModelCatalog.AiModelOption;
import static ru.otus.services.client.litellm.dto.AiModelCatalog.AiProviderOption;

@Service
@RequiredArgsConstructor
public class LiteLlmClient {

    private static final String URL = "/v1/models";

    @Qualifier("liteLlmRestClient")
    private final RestClient restClient;

    public AiModelCatalog getCatalogs() {
        LiteLlmModelsResponse response = restClient.get()
            .uri(URL)
            .retrieve()
            .body(LiteLlmModelsResponse.class);

        var models = response != null && response.data() != null ?
            response.data().stream()
                .map(it -> modelOf(it.id()))
                .sorted(Comparator.comparing(AiModelOption::provider).thenComparing(AiModelOption::displayName))
                .toList() :
            List.<AiModelOption>of();

        var providers = models.stream()
            .map(AiModelOption::provider)
            .distinct()
            .map(it -> AiProviderOption.builder()
                .id(it)
                .displayName(it)
                .build())
            .toList();

        return AiModelCatalog.builder().providers(providers).models(models).build();
    }

    private AiModelOption modelOf(String model) {
        int separator = model.indexOf('/');
        String provider = model.substring(0, separator).toLowerCase(getLocale());
        String modelName = model.substring(separator + 1);
        return AiModelOption.builder()
            .id(model)
            .provider(provider)
            .displayName(modelName)
            .build();
    }
}
