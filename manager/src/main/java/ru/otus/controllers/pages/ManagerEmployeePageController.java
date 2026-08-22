package ru.otus.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.dto.filter.ManagerUserFilter;
import ru.otus.dto.filter.ManagerUserFilter.EmployeeFilter;
import ru.otus.dto.filter.ProjectFilter;
import ru.otus.services.CareerLevelService;
import ru.otus.services.employee.ManagerEmployeeService;
import ru.otus.services.staffevaluation.ManagerStaffEvaluationService;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.project.ProjectService;
import ru.otus.services.SkillService;

@Controller
@RequiredArgsConstructor
public class ManagerEmployeePageController implements AbstractPageController {

    private static final String EMPLOYEE_DETAILS = "employeeDetails";

    private final ManagerEmployeeService managerEmployeeService;

    private final ManagerStaffEvaluationService managerStaffEvaluationService;

    private final ProjectService projectService;

    private final ProjectRoleService projectRoleService;

    private final CareerLevelService careerLevelService;

    private final SkillService skillService;

    @GetMapping("/employees")
    public String users(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long projectRole,
        @RequestParam(required = false) Long careerLevel,
        @RequestParam(required = false) Long skill,
        @RequestParam(required = false, name = PROJECT) Long projectId,
        @RequestParam(required = false, name = STAFF_EVALUATION) Long staffEvaluationId,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        var projectFilter = projectFilterOf(currentUser);
        var employeeFilter = EmployeeFilter.builder()
            .search(search)
            .projectRole(projectRole)
            .careerLevel(careerLevel)
            .skill(skill)
            .build();
        var userFilter = ManagerUserFilter.of(employeeFilter, projectId, staffEvaluationId, currentUser.id());
        var projects = projectService.findAll(projectFilter);
        var users = managerEmployeeService.findAll(userFilter);
        model.addAttribute(USERS, users);
        model.addAttribute(FILTER, userFilter);
        model.addAttribute(PROJECTS, projects);
        model.addAttribute(STAFF_EVALUATIONS, managerStaffEvaluationService.findAll());
        model.addAttribute(PROJECT_ROLES, projectRoleService.findAllEnabledValues());
        model.addAttribute(CAREER_LEVELS, careerLevelService.findAllEnabledValues());
        model.addAttribute(SKILLS, skillService.findAllEnabledValues());
        model.addAttribute(EXTRA_QUERY, userFilter.buildExtraQuery());
        return "page/employee/list";
    }

    @GetMapping("/employees/{employeeId}/detail")
    public String details(
        @PathVariable Long employeeId,
        @RequestParam(required = false) Long staffEvaluation,
        @RequestParam(required = false, name = PROJECT) Long projectId,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        var projectFilter = projectFilterOf(currentUser);
        var projects = projectService.findAll(projectFilter);
        model.addAttribute(EMPLOYEE_DETAILS, managerEmployeeService.findDetails(employeeId, staffEvaluation, projectId, currentUser.id()));
        model.addAttribute(PROJECTS, projects);
        model.addAttribute(STAFF_EVALUATIONS, managerStaffEvaluationService.findAllByEmployeeId(employeeId));
        return "page/employee/detail";
    }

    private static ProjectFilter projectFilterOf(CurrentUser currentUser) {
        return ProjectFilter.builder()
            .managerId(currentUser.id())
            .active(true)
            .build();
    }
}
