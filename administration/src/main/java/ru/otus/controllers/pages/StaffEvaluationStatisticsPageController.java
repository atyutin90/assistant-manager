package ru.otus.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.dto.filter.StaffEvaluationUserFilter;
import ru.otus.entity.enums.StaffEvaluationUserStatus;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.ValueListService;
import ru.otus.services.staffevaluation.StaffEvaluationService;
import ru.otus.services.staffevaluationuser.StaffEvaluationServiceUser;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class StaffEvaluationStatisticsPageController implements AbstractPageController {

    private static final String STATISTICS = "statistics";

    private static final String STATUSES = "statuses";

    private static final String STAFF_EVALUATION_USER_STATUS = "staff-evaluation-user-status";

    private final StaffEvaluationService staffEvaluationService;

    private final StaffEvaluationServiceUser staffEvaluationServiceUser;

    private final ProjectRoleService projectRoleService;

    private final ValueListService valueListService;

    @GetMapping("/staff-evaluations/{id}/statistics")
    public String statistics(@PathVariable Long id,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) StaffEvaluationUserStatus status,
                             @PageableDefault(page = DEFAULT_PAGE, sort = "user.lastName", direction = ASC)
                             Pageable pageable,
                             Model model) {
        var filter = StaffEvaluationUserFilter.builder()
            .staffEvaluationId(id)
            .search(search)
            .status(status)
            .build();
        var statistics = staffEvaluationServiceUser.findStatistics(filter, pageableOf(pageable));

        pageAttribute(model, pageable, statistics, filter);
        var statuses = valueListService.getValues(STAFF_EVALUATION_USER_STATUS);
        model.addAttribute(STAFF_EVALUATION, staffEvaluationService.findById(id));
        model.addAttribute(STATISTICS, statistics);
        model.addAttribute(PROJECT_ROLES, projectRoleService.findAllValues());
        model.addAttribute(STATUSES, statuses);
        model.addAttribute(FILTER, filter);
        model.addAttribute(SOURCE, "/staff-evaluations/%d/statistics".formatted(id));
        return "page/staff-evaluation/statistics";
    }
}
