package ru.otus.controllers.pages;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.dto.UserPasswordDto;
import ru.otus.services.ProfileService;
import ru.otus.services.StaffEvaluationUserService;
import ru.otus.services.VerificationService;

import static org.springframework.data.domain.Pageable.unpaged;

@Controller
@RequiredArgsConstructor
public class HomePageController implements AbstractPageController {

    private final static String PROFILE = "profile";

    private final static String VERIFICATIONS = "verifications";

    private final ProfileService profileService;

    private final StaffEvaluationUserService staffEvaluationUserService;

    private final VerificationService verificationService;

    @GetMapping("/")
    public String home(@CurrentUserParam CurrentUser currentUser,
                       Model model) {
        return homePage(model, currentUser, UserPasswordDto.builder().build(), false);
    }

    @PostMapping("/")
    public String changePassword(@Valid @ModelAttribute(PASSWORD_CHANGE) UserPasswordDto passwordChange,
                                 BindingResult bindingResult,
                                 @CurrentUserParam CurrentUser currentUser,
                                 Model model,
                                 RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return homePage(model, currentUser, passwordChange, true);
        }
        profileService.changePassword(currentUser.id(), passwordChange.newPassword());
        redirectAttributes.addFlashAttribute(SUCCESS_CHANGE_PASSWORD, true);
        return "redirect:/";
    }

    private String homePage(
        Model model,
        CurrentUser currentUser,
        UserPasswordDto passwordChange,
        boolean showPasswordModal
    ) {
        var userId = currentUser.id();
        var profile = profileService.getProfile(userId);
        var staffEvaluations = staffEvaluationUserService.findActive(userId);
        var verifications = verificationService.findPending(userId, unpaged()).getContent();
        model.addAttribute(PROFILE, profile);
        model.addAttribute(STAFF_EVALUATIONS, staffEvaluations);
        model.addAttribute(VERIFICATIONS, verifications);
        model.addAttribute(PASSWORD_CHANGE, passwordChange);
        model.addAttribute(SHOW_PASSWORD_MODAL, showPasswordModal);
        return "page/home/edit";
    }
}
