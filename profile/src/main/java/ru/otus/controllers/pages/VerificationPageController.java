package ru.otus.controllers.pages;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.dto.VerificationDetailsDto;
import ru.otus.dto.VerificationFormDto;
import ru.otus.services.verification.VerificationService;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Controller
@RequiredArgsConstructor
public class VerificationPageController implements AbstractPageController {

    private static final String VERIFICATIONS = "verifications";

    private static final String VERIFICATION = "verification";

    private static final String VERIFICATION_FORM = "verificationForm";

    private final VerificationService verificationService;

    @GetMapping("/verifications")
    public String list(
        @PageableDefault(page = DEFAULT_PAGE, direction = Sort.Direction.DESC) Pageable pageable,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        var verifications = verificationService.findPending(currentUser.id(), pageableOf(pageable));
        model.addAttribute(VERIFICATIONS, verifications);
        pageAttribute(model, pageable, verifications);
        return "page/verification/list";
    }

    @GetMapping("/verifications/{staffEvaluationUserId}")
    public String details(
        @PathVariable Long staffEvaluationUserId,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        var verification = verificationService.findDetails(staffEvaluationUserId, currentUser.id());
        model.addAttribute(VERIFICATION_FORM, VerificationFormDto.builder().build());
        enrichDetails(model, verification);
        return "page/verification/question";
    }

    @GetMapping("/verifications/{staffEvaluationUserId}/{uuid}")
    public String legacyQuestion(
        @PathVariable Long staffEvaluationUserId,
        @PathVariable String uuid
    ) {
        return verificationRedirect(staffEvaluationUserId, uuid);
    }

    @PostMapping("/verifications/{staffEvaluationUserId}/{uuid}")
    public String save(
        @PathVariable Long staffEvaluationUserId,
        @PathVariable String uuid,
        @Valid @ModelAttribute(VERIFICATION_FORM) VerificationFormDto form,
        BindingResult bindingResult,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            var verification = verificationService.findDetails(staffEvaluationUserId, currentUser.id());
            model.addAttribute("editingQuestionUuid", uuid);
            model.addAttribute("responseError", bindingResult.hasFieldErrors("response"));
            model.addAttribute("commentError", bindingResult.hasFieldErrors("comment"));
            enrichDetails(model, verification);
            return "page/verification/question";
        }
        verificationService.save(staffEvaluationUserId, currentUser.id(), form);
        return verificationRedirect(staffEvaluationUserId, uuid);
    }

    @PostMapping("/verifications/{staffEvaluationUserId}/confirm-all")
    public String confirmAll(
        @PathVariable Long staffEvaluationUserId,
        @CurrentUserParam CurrentUser currentUser
    ) {
        verificationService.confirmAll(staffEvaluationUserId, currentUser.id());
        return verificationRedirect(staffEvaluationUserId);
    }

    @PostMapping("/verifications/{staffEvaluationUserId}/complete")
    public String complete(
        @PathVariable Long staffEvaluationUserId,
        @CurrentUserParam CurrentUser currentUser
    ) {
        verificationService.complete(staffEvaluationUserId, currentUser.id());
        return "redirect:/verifications";
    }

    private String verificationRedirect(Long staffEvaluationUserId) {
        return verificationRedirect(staffEvaluationUserId, null);
    }

    private String verificationRedirect(Long staffEvaluationUserId, String questionUUID) {
        if (isNotEmpty(questionUUID)) {
            return "redirect:/verifications/%d#question-%s".formatted(staffEvaluationUserId,  questionUUID);
        } else {
            return "redirect:/verifications/%d".formatted(staffEvaluationUserId);
        }
    }

    private void enrichDetails(Model model, VerificationDetailsDto verification) {
        model.addAttribute(VERIFICATION, verification);
    }
}
