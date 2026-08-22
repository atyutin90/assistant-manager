package ru.otus.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.services.StaffEvaluationUserService;

@Controller
@RequiredArgsConstructor
public class StaffEvaluationPageController implements AbstractPageController {

    private static final String RESULT = "result";

    private final StaffEvaluationUserService staffEvaluationUserService;

    @GetMapping("/staff-evaluations")
    public String list(
        @PageableDefault(page = DEFAULT_PAGE) Pageable pageable,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        var staffEvaluations = staffEvaluationUserService.findAssigned(currentUser.id(), pageableOf(pageable));
        model.addAttribute(STAFF_EVALUATIONS, staffEvaluations);
        pageAttribute(model, pageable, staffEvaluations);
        return "page/staff-evaluations/list";
    }

    @GetMapping("/staff-evaluations/{staffEvaluationId}/detail")
    public String detail(
        @PathVariable Long staffEvaluationId,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        model.addAttribute(RESULT, staffEvaluationUserService.findResult(currentUser.id(), staffEvaluationId));
        return "page/staff-evaluations/detail";
    }
}
