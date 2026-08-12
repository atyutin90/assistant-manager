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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.dto.ProjectDto;
import ru.otus.dto.filter.ProjectFilter;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.services.project.ProjectService;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class ProjectPageController implements AbstractPageController {

    private static final String CAN_MANAGE_ACCESS = "canManageAccess";

    private static final String CAN_EDIT = "canEdit";

    private static final String CAN_DELETE = "canDelete";

    private final ProjectService projectService;

    @GetMapping("/projects")
    public String projects(
        @RequestParam(required = false) String search,
        @PageableDefault(page = DEFAULT_PAGE, sort = ID, direction = ASC) Pageable pageable,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        var filter = ProjectFilter.builder()
            .search(search)
            .managerId(currentUser.id())
            .build();
        var projects = projectService.findAll(filter, pageableOf(pageable));
        model.addAttribute(PROJECTS, projects);
        model.addAttribute(FILTER, filter);
        pageAttribute(model, pageable, projects, filter);
        return "page/project/list";
    }

    @GetMapping("/projects/new")
    public String create(@CurrentUserParam CurrentUser currentUser, Model model) {
        var project = ProjectDto.builder().active(true).build();
        formAttributes(project, currentUser, model);
        return "page/project/form";
    }

    @GetMapping("/projects/{id}")
    public String edit(@PathVariable Long id,
                       @CurrentUserParam CurrentUser currentUser,
                       Model model) {
        var project = projectService.findById(id, currentUser.id());
        formAttributes(project, currentUser, model);
        return "page/project/form";
    }

    @PostMapping("/projects")
    public String save(@Valid @ModelAttribute(PROJECT) ProjectDto project,
                       BindingResult bindingResult,
                       @CurrentUserParam CurrentUser currentUser,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                formAttributes(project, currentUser, model);
                return "page/project/form";
            }
            var saved = projectService.save(project, currentUser.id());
            redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
            return "redirect:/projects/%d".formatted(saved.id());
        } catch (NonUniqueValueException exception) {
            rejectFields(bindingResult, exception.getInfo());
            formAttributes(project, currentUser, model);
            return "page/project/form";
        }
    }

    @PostMapping("/projects/{id}")
    public String copy(@PathVariable Long id,
                       @CurrentUserParam CurrentUser currentUser,
                       RedirectAttributes redirectAttributes
    ) {
        projectService.copy(id, currentUser.id());
        redirectAttributes.addFlashAttribute(SUCCESS_COPY_OPERATION, true);
        return "redirect:/projects";
    }

    @DeleteMapping("/projects/{id}")
    public String delete(@PathVariable Long id,
                         @CurrentUserParam CurrentUser currentUser,
                         RedirectAttributes redirectAttributes
    ) {
        projectService.deleteById(id, currentUser.id());
        redirectAttributes.addFlashAttribute(SUCCESS_DELETE_OPERATION, true);
        return "redirect:/projects";
    }

    private void formAttributes(ProjectDto project, CurrentUser currentUser, Model model) {
        boolean isEdit = project.id() != null;
        boolean canDelete = currentUser.id().equals(project.owner() != null ? project.owner().id() : null);
        boolean canEdit = !isEdit || canDelete || projectService.canEdit(project.id(), currentUser.id());
        model.addAttribute(PROJECT, project);
        model.addAttribute(IS_EDIT, isEdit);
        model.addAttribute(CAN_EDIT, canEdit);
        model.addAttribute(CAN_MANAGE_ACCESS, isEdit && canEdit);
        model.addAttribute(CAN_DELETE, canDelete);
    }
}
