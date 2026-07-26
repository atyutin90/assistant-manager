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
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.dto.VerificationDetailsDto;
import ru.otus.dto.VerificationFormDto;
import ru.otus.services.VerificationService;

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
    public String start(
        @PathVariable Long staffEvaluationUserId,
        @CurrentUserParam CurrentUser currentUser
    ) {
        var questionUuid = verificationService.findStartQuestion(staffEvaluationUserId, currentUser.id());
        return verificationRedirect(staffEvaluationUserId, questionUuid);
    }

    @GetMapping("/verifications/{staffEvaluationUserId}/{uuid}")
    public String details(
        @PathVariable Long staffEvaluationUserId,
        @PathVariable String uuid,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        var verification = verificationService.findDetails(staffEvaluationUserId, currentUser.id(), uuid);
        model.addAttribute(VERIFICATION_FORM, formOf(verification));
        enrichDetails(model, verification);
        return "page/verification/question";
    }

    @PostMapping("/verifications/{staffEvaluationUserId}/{uuid}")
    public String save(
        @PathVariable Long staffEvaluationUserId,
        @PathVariable String uuid,
        @RequestParam String action,
        @Valid @ModelAttribute(VERIFICATION_FORM) VerificationFormDto form,
        BindingResult bindingResult,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            var verification = verificationService.findDetails(staffEvaluationUserId, currentUser.id(), uuid);
            enrichDetails(model, verification);
            return "page/verification/question";
        }
        var finish = FINISH.equals(action);
        verificationService.save(staffEvaluationUserId, currentUser.id(), form, finish);
        return finish ? "redirect:/verifications" : verificationRedirect(staffEvaluationUserId);
    }

    private VerificationFormDto formOf(VerificationDetailsDto verification) {
        return VerificationFormDto.builder()
            .answerId(verification.currentQuestion().answerId())
            .response(verification.currentQuestion().verifiedResponse())
            .comment(verification.currentQuestion().comment())
            .build();
    }

    private String verificationRedirect(Long staffEvaluationUserId) {
        return "redirect:/verifications/%d".formatted(staffEvaluationUserId);
    }

    private String verificationRedirect(Long staffEvaluationUserId, String questionUuid) {
        return "redirect:/verifications/%d/%s".formatted(staffEvaluationUserId, questionUuid);
    }

    private void enrichDetails(Model model, VerificationDetailsDto verification) {
        model.addAttribute(VERIFICATION, verification);
    }
}
