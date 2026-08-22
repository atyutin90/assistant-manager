package ru.otus.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.dto.filter.StaffEvaluationUserFilter;
import ru.otus.dto.filter.UserFilter;
import ru.otus.dto.page.PageDataFilter;
import ru.otus.entity.enums.UserRole;
import ru.otus.services.CareerLevelService;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.UserService;
import ru.otus.services.staffevaluation.StaffEvaluationService;
import ru.otus.services.staffevaluationuser.StaffEvaluationServiceUser;

import java.util.Set;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class StaffEvaluationEmployeePageController implements StaffEvaluationController {

    private static final String PROJECT_ROLE = "project_role";

    private static final String EMPLOYEE_SEARCH = "employee_search";

    private static final String EMPLOYEE_PROJECT_ROLE = "employee_project_role";

    private static final String ASSIGNED_EMPLOYEES = "assignedEmployees";

    private static final String ASSIGNED_PAGE = "assigned";

    private static final String EMPLOYEE_PAGE = "employee";

    private static final String EMPLOYEE_FILTER = "employeeFilter";

    private static final String ASSIGNED_FILTER = "assignedFilter";

    private static final String ASSIGNED_EXTRA_QUERY = "assignedExtraQuery";

    private static final String EMPLOYEE_EXTRA_QUERY = "employeeExtraQuery";

    private final StaffEvaluationService staffEvaluationService;

    private final StaffEvaluationServiceUser staffEvaluationServiceUser;

    private final UserService userService;

    private final ProjectRoleService projectRoleService;

    private final CareerLevelService careerLevelService;

    @GetMapping("/staff-evaluations/{id}/employees")
    public String employees(@PathVariable Long id,
                            @RequestParam(required = false, name = EMPLOYEE_SEARCH) String employeeSearch,
                            @RequestParam(required = false, name = EMPLOYEE_PROJECT_ROLE) Long employeeProjectRole,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false, name = PROJECT_ROLE) Long projectRole,
                            @RequestParam(required = false, defaultValue = "false") boolean modal,
                            @Qualifier(ASSIGNED_PAGE)
                            @PageableDefault(page = DEFAULT_PAGE, sort = "user.lastName", direction = ASC)
                            Pageable assignedPageable,
                            @Qualifier(EMPLOYEE_PAGE)
                            @PageableDefault(page = DEFAULT_PAGE, sort = "lastName", direction = ASC)
                            Pageable employeePageable,
                            Model model) {
        var employeeFilter = UserFilter.builder()
            .search(employeeSearch)
            .projectRole(employeeProjectRole)
            .role(UserRole.USER)
            .build();

        var assignedFilter = StaffEvaluationUserFilter.builder()
            .search(search)
            .projectRole(projectRole)
            .staffEvaluationId(id)
            .build();

        var staffEvaluation = staffEvaluationService.findEmployeeById(id);
        var employees = userService.findAll(employeeFilter, pageableOf(employeePageable));
        var assignedEmployees = staffEvaluationServiceUser.findAll(assignedFilter, pageableOf(assignedPageable));
        enrichment(model);
        addPageAttributes(model, ASSIGNED_PAGE, assignedPageable, assignedEmployees);
        addPageAttributes(model, EMPLOYEE_PAGE, employeePageable, employees);
        model.addAttribute(ASSIGNED_EXTRA_QUERY, buildAssignedQuery(assignedFilter));
        model.addAttribute(EMPLOYEE_EXTRA_QUERY, buildEmployeeQuery(employeeFilter));
        model.addAttribute(STAFF_EVALUATION, staffEvaluation);
        model.addAttribute(ASSIGNED_EMPLOYEES, assignedEmployees);
        model.addAttribute(EMPLOYEES, employees);
        model.addAttribute(EMPLOYEE_FILTER, employeeFilter);
        model.addAttribute(ASSIGNED_FILTER, assignedFilter);
        model.addAttribute(MODAL, modal);
        model.addAttribute(SOURCE, "/staff-evaluations/%d/employees".formatted(id));
        return "page/staff-evaluation/employees";
    }

    @PostMapping("/staff-evaluations/{id}/employees/selected")
    public String addSelectedEmployees(@PathVariable Long id,
                                       @RequestParam(required = false) Set<Long> employeeIds) {
        if (isNotEmpty(employeeIds)) {
            staffEvaluationService.addEmployees(id, employeeIds);
        }
        return redirect(id);
    }

    @PostMapping("/staff-evaluations/{id}/employees")
    public String addFilteredEmployees(@PathVariable Long id,
                                       @RequestParam(required = false, name = EMPLOYEE_SEARCH) String search,
                                       @RequestParam(required = false, name = EMPLOYEE_PROJECT_ROLE) Long projectRole) {
        var filter = UserFilter.builder()
            .search(search)
            .projectRole(projectRole)
            .role(UserRole.USER)
            .build();
        staffEvaluationService.addEmployees(id, filter);
        return redirect(id);
    }

    @DeleteMapping("/staff-evaluations/{id}/employees/selected")
    public String removeSelectedEmployees(@PathVariable Long id,
                                          @RequestParam(required = false, name = SEARCH) String search,
                                          @RequestParam(required = false, name = PROJECT_ROLE) Long projectRole,
                                          @RequestParam(required = false) Set<Long> employeeIds) {
        if (isNotEmpty(employeeIds)) {
            staffEvaluationService.removeEmployees(id, employeeIds);
        }
        return redirect(id, StaffEvaluationUserFilter.builder().search(search).projectRole(projectRole).build());
    }

    @DeleteMapping("/staff-evaluations/{id}/employees")
    public String removeFilteredEmployees(@PathVariable Long id,
                                          @RequestParam(required = false, name = SEARCH) String search,
                                          @RequestParam(required = false, name = PROJECT_ROLE) Long projectRole) {
        var filter = UserFilter.builder()
            .search(search)
            .projectRole(projectRole)
            .role(UserRole.USER)
            .build();
        staffEvaluationService.removeEmployees(id, filter);
        return redirect(id);
    }


    private void enrichment(Model model) {
        model.addAttribute(PROJECT_ROLES, projectRoleService.findAllValues());
        model.addAttribute(CAREER_LEVELS, careerLevelService.findAllValues());
    }

    private String redirect(Long id) {
        return "redirect:/staff-evaluations/%d/employees".formatted(id);
    }

    private String redirect(Long id, StaffEvaluationUserFilter filter) {
        var url = new StringBuilder();
        url.append(redirect(id));
        var queryParams = buildAssignedQuery(filter);
        if (StringUtils.isNotEmpty(queryParams)) {
            url.append("?").append(queryParams.substring(1));
        }
        return url.toString();
    }

    private String buildEmployeeQuery(UserFilter filter) {
        return buildEmployeeQuery(filter.search(), filter.projectRole(), filter);
    }

    private String buildEmployeeQuery(String search, Long projectRole, PageDataFilter filter) {
        StringBuilder query = new StringBuilder();
        filter.appendQueryParam(query, EMPLOYEE_SEARCH, search);
        filter.appendQueryParam(query, EMPLOYEE_PROJECT_ROLE, projectRole);
        filter.appendQueryParam(query, MODAL, true);
        return query.toString();
    }

    private String buildAssignedQuery(StaffEvaluationUserFilter filter) {
        return buildAssignedQuery(filter.search(), filter.projectRole(), filter);
    }

    private String buildAssignedQuery(String search, Long projectRole, PageDataFilter filter) {
        StringBuilder query = new StringBuilder();
        filter.appendQueryParam(query, SEARCH, search);
        filter.appendQueryParam(query, PROJECT_ROLE, projectRole);
        return query.toString();
    }
}
