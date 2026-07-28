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
import ru.otus.dto.StaffEvaluationDto;
import ru.otus.dto.filter.StaffEvaluationFilter;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.exceptions.StaffEvaluationStatusException;
import ru.otus.services.staffevaluation.StaffEvaluationService;

import static ru.otus.entity.enums.StaffEvaluationStatus.DRAFT;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class StaffEvaluationPageController implements AbstractPageController {

    private final StaffEvaluationService staffEvaluationService;

    @GetMapping("/staff-evaluations")
    public String list(@RequestParam(required = false) String search,
                       @PageableDefault(page = DEFAULT_PAGE, sort = ID, direction = ASC) Pageable pageable,
                       Model model) {
        var filter = StaffEvaluationFilter.builder().search(search).build();
        var staffEvaluations = staffEvaluationService.findAll(filter, pageableOf(pageable));
        pageAttribute(model, pageable, staffEvaluations, filter);
        model.addAttribute(STAFF_EVALUATIONS, staffEvaluations);
        model.addAttribute(FILTER, filter);
        return "page/staff-evaluation/list";
    }

    @GetMapping("/staff-evaluations/new")
    public String create(Model model) {
        model.addAttribute(STAFF_EVALUATION, StaffEvaluationDto.builder().status(DRAFT).build());
        model.addAttribute(IS_EDIT, false);
        return "page/staff-evaluation/form";
    }

    @GetMapping("/staff-evaluations/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute(STAFF_EVALUATION, staffEvaluationService.findById(id));
        model.addAttribute(IS_EDIT, true);
        return "page/staff-evaluation/form";
    }

    @PostMapping("/staff-evaluations")
    public String create(@Valid @ModelAttribute(STAFF_EVALUATION) StaffEvaluationDto staffEvaluation,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute(IS_EDIT, staffEvaluation.id() != null);
                return "page/staff-evaluation/form";
            }
            if (staffEvaluation.id() != null) {
                staffEvaluationService.update(staffEvaluation);
            } else {
                staffEvaluation = staffEvaluationService.create(staffEvaluation);
            }
            redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
            return "redirect:/staff-evaluations/%d".formatted(staffEvaluation.id());
        } catch (NonUniqueValueException ex) {
            rejectFields(bindingResult, ex.getInfo());
            model.addAttribute(IS_EDIT, staffEvaluation.id() != null);
            return "page/staff-evaluation/form";
        }
    }

    @DeleteMapping("/staff-evaluations/{id}/delete")
    public String delete(@PathVariable Long id) {
        staffEvaluationService.deleteById(id);
        return "redirect:/staff-evaluations";
    }

    @PostMapping("/staff-evaluations/{id}/start")
    public String start(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            staffEvaluationService.start(id);
        } catch (StaffEvaluationStatusException exception) {
            redirectAttributes.addFlashAttribute(ERROR, exception.getMessage());
        }
        return redirectToEvaluation(id);
    }

    @PostMapping("/staff-evaluations/{id}/complete")
    public String complete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            staffEvaluationService.complete(id);
        } catch (StaffEvaluationStatusException exception) {
            redirectAttributes.addFlashAttribute(ERROR, exception.getMessage());
        }
        return redirectToEvaluation(id);
    }

    private String redirectToEvaluation(Long id) {
        return "redirect:/staff-evaluations/%d".formatted(id);
    }
}
