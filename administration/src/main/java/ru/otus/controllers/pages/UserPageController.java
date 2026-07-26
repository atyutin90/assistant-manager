package ru.otus.controllers.pages;

import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.dto.CsvUserDto;
import ru.otus.dto.UserDto;
import ru.otus.dto.UserPasswordDto;
import ru.otus.dto.filter.UserFilter;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.services.CareerLevelService;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.UserService;
import ru.otus.services.csv.CsvService;
import ru.otus.services.ValueListService;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class UserPageController implements DownloadPageController {

    private final static String USER_ROLE = "user-role";

    private final UserService userService;

    private final ValueListService valueListService;

    private final ProjectRoleService projectRoleService;

    private final CareerLevelService careerLevelService;

    private final CsvService csvService;

    @GetMapping("/users")
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) Long projectRole,
                       @PageableDefault(page = DEFAULT_PAGE, sort = ID, direction = ASC) Pageable pageable,
                       Model model) {
        var filter = UserFilter.builder().search(search).projectRole(projectRole).build();
        var users = userService.findAll(filter, pageableOf(pageable));
        enrichment(model);
        pageAttribute(model, pageable, users, filter);
        model.addAttribute(USERS, users);
        model.addAttribute(FILTER, filter);
        return "page/user/list";
    }

    @GetMapping("/users/new")
    public String create(Model model) {
        model.addAttribute(USER, UserDto.builder().build());
        model.addAttribute(IS_EDIT, false);
        enrichment(model);
        return "page/user/form";
    }

    @GetMapping("/users/{id}")
    public String edit(@PathVariable Long id, Model model) {
        return editPage(id, model, UserPasswordDto.builder().build(), false);
    }

    @PostMapping("/users/{id}")
    public String changePassword(@PathVariable Long id,
                                 @Valid @ModelAttribute(PASSWORD_CHANGE) UserPasswordDto passwordChange,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return editPage(id, model, passwordChange, true);
        }

        userService.changePassword(id, passwordChange.newPassword());
        redirectAttributes.addFlashAttribute(SUCCESS_CHANGE_PASSWORD, true);
        return "redirect:/users/{id}";
    }

    @PostMapping("/users")
    public String create(@Valid @ModelAttribute(USER) UserDto employee,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute(IS_EDIT, false);
                enrichment(model);
                return "page/user/form";
            }
            if (employee.id() != null) {
                userService.update(employee);
            } else {
                employee = userService.create(employee);
            }
            redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
            return "redirect:/users/%d".formatted(employee.id());

        } catch (NonUniqueValueException ex) {
            rejectFields(bindingResult, ex.getInfo());
            model.addAttribute(IS_EDIT, false);
            enrichment(model);
            return "page/user/form";
        }
    }

    @DeleteMapping("/users/{id}/delete")
    public String delete(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/users";
    }

    @PostMapping("/users/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        csvService.uploadUsers(file);
        return "redirect:/users";
    }

    @GetMapping("/users/download")
    public void download(@RequestParam(required = false) String search,
                         @RequestParam(required = false) Long projectRole,
                         HttpServletResponse response) throws Exception {
        var csvEmployees = csvService.downloadUsers(UserFilter.builder()
            .search(search)
            .projectRole(projectRole)
            .build());
        download(response, csvEmployees, CsvUserDto.class, "users.csv");
    }

    private String editPage(Long id,
                            Model model,
                            UserPasswordDto passwordChange,
                            boolean showPasswordModal) {
        model.addAttribute(USER, userService.findById(id));
        model.addAttribute(IS_EDIT, true);
        model.addAttribute(PASSWORD_CHANGE, passwordChange);
        model.addAttribute(SHOW_PASSWORD_MODAL, showPasswordModal);
        enrichment(model, id);
        return "page/user/form";
    }

    private void enrichment(Model model) {
        var userRoles = valueListService.getValues(USER_ROLE);
        model.addAttribute(PROJECT_ROLES, projectRoleService.findAllValues());
        model.addAttribute(CAREER_LEVELS, careerLevelService.findAllValues());
        model.addAttribute(USER_ROLES, userRoles);
    }

    private void enrichment(Model model, Long id) {
        enrichment(model);
        model.addAttribute(TEAM_LEADS, userService.findTeamLeads(id));
    }
}
