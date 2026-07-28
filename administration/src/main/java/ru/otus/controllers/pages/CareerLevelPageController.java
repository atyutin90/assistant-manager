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
import ru.otus.dto.CareerLevelDto;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.services.CareerLevelService;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class CareerLevelPageController implements AbstractPageController {

    private final CareerLevelService careerLevelService;

    @GetMapping("/career-levels")
    public String list(@PageableDefault(page = DEFAULT_PAGE, sort = ID, direction = ASC) Pageable pageable,
                       Model model) {
        var careerLevels = careerLevelService.findAll(pageableOf(pageable));
        pageAttribute(model, pageable, careerLevels);
        model.addAttribute(CAREER_LEVELS, careerLevels);
        return "page/manual/career-level/list";
    }

    @GetMapping("/career-levels/new")
    public String create(Model model) {
        model.addAttribute(CAREER_LEVEL, CareerLevelDto.builder().build());
        model.addAttribute(IS_EDIT, false);
        return "page/manual/career-level/form";
    }

    @GetMapping("/career-levels/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute(CAREER_LEVEL, careerLevelService.findById(id));
        model.addAttribute(IS_EDIT, true);
        return "page/manual/career-level/form";
    }

    @PostMapping("/career-levels")
    private String save(@Valid @ModelAttribute(CAREER_LEVEL) CareerLevelDto careerLevel,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(IS_EDIT, careerLevel.id() != null);
            return "page/manual/career-level/form";
        }
        try {
            if (careerLevel.id() != null) {
                careerLevelService.update(careerLevel);
            } else {
                careerLevel = careerLevelService.create(careerLevel);
            }
            redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
            return "redirect:/career-levels/%d".formatted(careerLevel.id());
        } catch (NonUniqueValueException ex) {
            rejectFields(bindingResult, ex.getInfo());
            model.addAttribute(IS_EDIT, careerLevel.id() != null);
            return "page/manual/career-level/form";
        }
    }

    @DeleteMapping("/career-levels/{id}")
    private String delete(@PathVariable long id) {
        careerLevelService.deleteById(id);
        return "redirect:/career-levels";
    }
}
