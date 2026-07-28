package ru.otus.controllers.pages;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.dto.ProjectRoleDto;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.services.ProjectRoleService;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class ProjectRolePageController implements AbstractPageController {

    private final ProjectRoleService projectRoleService;

    @GetMapping("/project-roles")
    public String list(@PageableDefault(page = DEFAULT_PAGE, sort = ID, direction = ASC) Pageable pageable,
                       Model model) {
        var projectRoles = projectRoleService.findAll(pageableOf(pageable));
        pageAttribute(model, pageable, projectRoles);
        model.addAttribute(PROJECT_ROLES, projectRoles);
        return "page/manual/project-role/list";
    }

    @GetMapping("/project-roles/new")
    public String create(Model model) {
        model.addAttribute(PROJECT_ROLE, ProjectRoleDto.builder().build());
        model.addAttribute(IS_EDIT, false);
        return "page/manual/project-role/form";
    }

    @GetMapping("/project-roles/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute(PROJECT_ROLE, projectRoleService.findById(id));
        model.addAttribute(IS_EDIT, true);
        return "page/manual/project-role/form";
    }

    @PostMapping("/project-roles")
    public String save(@Valid @ModelAttribute(PROJECT_ROLE) ProjectRoleDto projectRole,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(IS_EDIT, projectRole.id() != null);
            return "page/manual/project-role/form";
        }
        try {
            if (projectRole.id() != null) {
                projectRoleService.update(projectRole);
            } else {
                projectRole = projectRoleService.create(projectRole);
            }
            redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
            return "redirect:/project-roles/%d".formatted(projectRole.id());
        } catch (NonUniqueValueException ex) {
            rejectFields(bindingResult, ex.getInfo());
            model.addAttribute(IS_EDIT, projectRole.id() != null);
            return "page/manual/project-role/form";
        }
    }

    @DeleteMapping("/project-roles/{id}")
    public String delete(@PathVariable long id) {
        projectRoleService.deleteById(id);
        return "redirect:/project-roles";
    }
}
