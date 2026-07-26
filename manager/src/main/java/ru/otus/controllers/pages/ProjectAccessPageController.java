package ru.otus.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.services.ProjectService;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class ProjectAccessPageController implements AbstractPageController {

    private final ProjectService projectService;

    @GetMapping("/projects/{id}/access")
    public String access(@PathVariable Long id,
                         @CurrentUserParam CurrentUser currentUser,
                         Model model) {
        var userId = currentUser.id();
        model.addAttribute(PROJECT, projectService.findByIdAndOwnerId(id, userId));
        model.addAttribute(MANAGERS, projectService.findManagerAccessOptions(id, userId));
        return "page/project/access";
    }

    @PostMapping("/projects/{id}/access")
    public String saveAccess(
        @PathVariable Long id,
        @RequestParam(required = false) Set<Long> managerIds,
        @CurrentUserParam CurrentUser currentUser,
        RedirectAttributes redirectAttributes
    ) {
        projectService.saveAccess(id, currentUser.id(), managerIds);
        redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
        return "redirect:/projects/%d/access".formatted(id);
    }
}
