package ru.otus.services.ai;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.dto.AiManagerSettingForm;
import ru.otus.entity.ManagerAiSetting;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.repositories.ManagerAiSettingRepository;
import ru.otus.repositories.UserRepository;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static ru.otus.exceptions.WebApplicationException.errorOf;

@Service
@RequiredArgsConstructor
public class ManagerAiSettingServiceImpl implements ManagerAiSettingService {

    private final ManagerAiSettingRepository managerAiSettingRepository;

    private final UserRepository userRepository;

    private final LiteLlmModelCatalogService modelCatalogService;

    private final MessageSource messageSource;

    @Override
    public AiManagerSettingForm findSetting(Long managerId) {
        return managerAiSettingRepository.findByManagerId(managerId)
            .map(setting ->
                AiManagerSettingForm.builder()
                    .provider(setting.getProvider())
                    .model(setting.getModel())
                    .apiKey(setting.getApiKey())
                    .build()
            ).orElse(AiManagerSettingForm.builder().build());
    }

    @Override
    public boolean hasSetting(Long managerId) {
        return managerAiSettingRepository.findByManagerId(managerId).isPresent();
    }

    @Override
    public boolean hasApiKey(Long managerId, AiManagerSettingForm form) {
        return hasApiKey(managerId, form.provider(), form.model());
    }

    @Override
    public boolean hasApiKey(Long managerId, String provider, String model) {
        return managerAiSettingRepository.findByManagerId(managerId)
            .filter(setting -> setting.getProvider().equals(provider))
            .filter(setting -> setting.getModel().equals(model))
            .map(ManagerAiSetting::getApiKey)
            .filter(StringUtils::isNotBlank)
            .isPresent();
    }

    @Transactional
    @Override
    public void delete(Long managerId) {
        var managerAiSetting = managerAiSettingRepository.findByManagerId(managerId)
            .orElseThrow(() -> notFoundException(managerId));
        managerAiSettingRepository.delete(managerAiSetting);
    }

    @Transactional
    @Override
    public void save(Long managerId, AiManagerSettingForm form) {
        var setting = managerAiSettingRepository.findByManagerId(managerId)
            .orElseGet(() ->
                ManagerAiSetting.builder()
                    .manager(userRepository.getReferenceById(managerId))
                    .build());
        var catalog = modelCatalogService.getCatalog();
        if (!catalog.contains(form.provider(), form.model())) {
            throw errorOf(
                BAD_REQUEST,
                messageSource.getMessage("error.ai-model-is-unavailable", new Object[]{form.model()},
                getLocale()));
        }
        boolean providerChanged = setting.getId() != null && !setting.getProvider().equals(form.provider());

        setting.setProvider(form.provider());
        setting.setModel(form.model());
        if (isNotBlank(form.apiKey())) {
            setting.setApiKey(form.apiKey().trim());
        } else if (providerChanged) {
            setting.setApiKey(null);
        }
        managerAiSettingRepository.save(setting);
    }

    private DataNotFoundException notFoundException(Long id) {
        return new DataNotFoundException(
            messageSource.getMessage("error.not-found-ai-setting-for-manager", new Object[]{id}, getLocale()));
    }
}
