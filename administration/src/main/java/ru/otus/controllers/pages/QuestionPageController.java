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
import ru.otus.dto.CsvQuestionDto;
import ru.otus.dto.QuestionDto;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.SkillService;
import ru.otus.services.csv.CsvService;
import ru.otus.services.question.QuestionService;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class QuestionPageController implements DownloadPageController {

    private final QuestionService questionService;

    private final ProjectRoleService projectRoleService;

    private final SkillService skillService;

    private final CsvService csvService;

    @SuppressWarnings("checkstyle:ParameterNumber")
    @GetMapping("/questions")
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) Long projectRole,
                       @RequestParam(required = false) Long skill,
                       @RequestParam(required = false) Boolean enabled,
                       @PageableDefault(page = DEFAULT_PAGE, sort = ID, direction = ASC) Pageable pageable,
                       Model model) {
        var filter = QuestionFilter.builder()
            .enabled(enabled)
            .search(search)
            .skill(skill)
            .projectRole(projectRole)
            .build();
        var questions = questionService.findAll(filter, pageableOf(pageable));
        model.addAttribute(QUESTIONS, questions);
        model.addAttribute(FILTER, filter);
        enrichment(model);
        pageAttribute(model, pageable, questions, filter);
        return "page/question/list";
    }

    @GetMapping("/questions/new")
    public String create(Model model) {
        model.addAttribute(QUESTION, QuestionDto.builder().enabled(true).build());
        model.addAttribute(IS_EDIT, false);
        enrichment(model);
        return "page/question/form";
    }

    @GetMapping("/questions/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute(QUESTION, questionService.findById(id));
        model.addAttribute(IS_EDIT, true);
        enrichment(model);
        return "page/question/form";
    }

    @PostMapping("/questions")
    public String create(@Valid @ModelAttribute(QUESTION) QuestionDto question,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute(IS_EDIT, question.id() != null);
                enrichment(model);
                return "page/question/form";
            }
            if (question.id() != null) {
                questionService.update(question);
            } else {
                question = questionService.create(question);
            }
            redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
            return "redirect:/questions/%d".formatted(question.id());
        } catch (NonUniqueValueException ex) {
            rejectFields(bindingResult, ex.getInfo());
            model.addAttribute(IS_EDIT, question.id() != null);
            enrichment(model);
            return "page/question/form";
        }
    }

    @DeleteMapping("/questions/{id}/delete")
    public String delete(@PathVariable Long id) {
        questionService.deleteById(id);
        return "redirect:/questions";
    }

    @PostMapping("/questions/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        csvService.uploadQuestions(file);
        return "redirect:/questions";
    }

    @GetMapping("/questions/download")
    public void download(@RequestParam(required = false) String search,
                         @RequestParam(required = false) Long projectRole,
                         @RequestParam(required = false) Long skill,
                         @RequestParam(required = false) Boolean enabled,
                         HttpServletResponse response) throws Exception {
        var csvQuestions = csvService.downloadQuestions(QuestionFilter.builder()
            .enabled(enabled)
            .search(search)
            .skill(skill)
            .projectRole(projectRole)
            .build());
        download(response, csvQuestions, CsvQuestionDto.class, "questions.csv");
    }

    private void enrichment(Model model) {
        model.addAttribute(PROJECT_ROLES, projectRoleService.findAllValues());
        model.addAttribute(SKILLS, skillService.findAllValues());
    }
}
