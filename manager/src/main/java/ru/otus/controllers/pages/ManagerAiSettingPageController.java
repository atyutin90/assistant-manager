package ru.otus.controllers.pages;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.AiManagerSettingForm;
import ru.otus.dto.AiModelCatalogDto;
import ru.otus.dto.CurrentUser;
import ru.otus.services.ai.LiteLlmModelCatalogService;
import ru.otus.services.ai.ManagerAiSettingService;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Controller
@RequiredArgsConstructor
public class ManagerAiSettingPageController implements AbstractPageController {

    private static final String CAN_DELETE = "canDelete";

    private static final String FORM = "settings";

    private final ManagerAiSettingService managerAiSettingService;

    private final LiteLlmModelCatalogService modelCatalogService;

    @GetMapping("/settings/ai")
    public String page(@CurrentUserParam CurrentUser currentUser, Model model) {
        var settingForm = managerAiSettingService.findSetting(currentUser.id());
        model.addAttribute(FORM, managerAiSettingService.findSetting(currentUser.id()));
        model.addAttribute(CAN_DELETE, settingForm.isApiKeyConfigured());
        addModels(model, modelCatalogService.getCatalog());
        return "page/setting/ai";
    }

    @DeleteMapping("/settings/ai")
    public String delete(@CurrentUserParam CurrentUser currentUser, RedirectAttributes redirectAttributes) {
        managerAiSettingService.delete(currentUser.id());
        redirectAttributes.addFlashAttribute(SUCCESS_DELETE_OPERATION, true);
        return "redirect:/settings/ai";
    }

    @PostMapping("/settings/ai")
    public String save(
        @CurrentUserParam CurrentUser currentUser,
        @Valid @ModelAttribute(FORM) AiManagerSettingForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        var catalog = modelCatalogService.getCatalog();
        if (isNotBlank(form.model()) && isNotBlank(form.provider()) && !catalog.contains(form.provider(), form.model())) {
            bindingResult.rejectValue("model", "ai-setting.model.invalid-provider");
        }
        if (isNotBlank(form.provider()) && (isBlank(form.apiKey()) && !managerAiSettingService.hasApiKey(currentUser.id(), form))) {
            bindingResult.rejectValue("apiKey", "ai-setting.api-key.required");
        }
        if (bindingResult.hasErrors()) {
            addModels(model, catalog);
            return "page/setting/ai";
        }
        managerAiSettingService.save(currentUser.id(), form);
        redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
        return "redirect:/settings/ai";
    }

    private static void addModels(Model model, AiModelCatalogDto catalog) {
        model.addAttribute("providers", catalog.providers());
        model.addAttribute("models", catalog.models());
    }
}
