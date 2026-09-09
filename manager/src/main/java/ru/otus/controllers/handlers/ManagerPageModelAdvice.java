package ru.otus.controllers.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.otus.controllers.pages.AbstractPageController;
import ru.otus.services.ai.ManagerAiSettingService;

import java.security.Principal;

@ControllerAdvice(basePackageClasses = AbstractPageController.class)
@RequiredArgsConstructor
public class ManagerPageModelAdvice {

    public static final String AI_SEARCH_AVAILABLE = "aiSearchAvailable";

    private final ManagerAiSettingService managerAiSettingService;

    @ModelAttribute(AI_SEARCH_AVAILABLE)
    public boolean isAiSearchAvailable(Principal principal) {
        return principal != null && managerAiSettingService.hasSetting(principal.getName());
    }
}
