package ru.otus.services.ai;

import ru.otus.dto.AiManagerSettingForm;

public interface ManagerAiSettingService {

    AiManagerSettingForm findSetting(Long managerId);

    boolean hasSetting(String managerUsername);

    boolean hasApiKey(Long managerId, AiManagerSettingForm form);

    boolean hasApiKey(Long managerId, String provider, String model);

    void delete(Long managerId);

    void save(Long managerId, AiManagerSettingForm form);
}
