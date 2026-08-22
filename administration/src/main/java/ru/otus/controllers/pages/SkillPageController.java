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
import ru.otus.dto.SkillDto;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.services.SkillService;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class SkillPageController implements AbstractPageController {

    private final SkillService skillService;

    @GetMapping("/skills")
    public String list(@PageableDefault(page = DEFAULT_PAGE, sort = ID, direction = ASC) Pageable pageable,
                       Model model) {
        var skills = skillService.findAll(pageableOf(pageable));
        pageAttribute(model, pageable, skills);
        model.addAttribute(SKILLS, skills);
        return "page/manual/skill/list";
    }

    @GetMapping("/skills/new")
    public String create(Model model) {
        model.addAttribute(SKILL, SkillDto.builder().build());
        model.addAttribute(IS_EDIT, false);
        return "page/manual/skill/form";
    }

    @GetMapping("/skills/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute(SKILL, skillService.findById(id));
        model.addAttribute(IS_EDIT, true);
        return "page/manual/skill/form";
    }

    @PostMapping("/skills")
    public String save(@Valid @ModelAttribute(SKILL) SkillDto skill,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(IS_EDIT, skill.id() != null);
            return "page/manual/skill/form";
        }
        try {
            if (skill.id() != null) {
                skillService.update(skill);
            } else {
                skill = skillService.create(skill);
            }
            redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
            return "redirect:/skills/%d".formatted(skill.id());
        } catch (NonUniqueValueException ex) {
            rejectFields(bindingResult, ex.getInfo());
            model.addAttribute(IS_EDIT, skill.id() != null);
            return "page/manual/skill/form";
        }
    }

    @DeleteMapping("/skills/{id}")
    public String delete(@PathVariable long id) {
        skillService.deleteById(id);
        return "redirect:/skills";
    }
}
