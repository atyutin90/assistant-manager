package ru.otus.controllers.pages;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.dto.CsvProjectQuestionDto;
import ru.otus.dto.ProjectQuestionLevelDto;
import ru.otus.dto.ProjectQuestionsForm;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.services.CareerLevelService;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.csv.ProjectQuestionCsvService;
import ru.otus.services.project.ProjectService;
import ru.otus.services.SkillService;

import static java.lang.Math.max;

@Controller
@RequiredArgsConstructor
public class ProjectQuestionPageController implements DownloadPageController {

    private static final String QUESTION_LEVELS = "questionLevels";

    private static final String QUESTION_PAGE = "questionPage";

    private static final String QUESTION_PAGE_SOURCE = "questionPageSource";

    private static final String CAN_EDIT = "canEdit";

    private static final String EXPORT_FILE_NAME = "project-questions.csv";

    private static final int QUESTION_PAGE_SIZE = 100;

    private final ProjectService projectService;

    private final ProjectRoleService projectRoleService;

    private final CareerLevelService careerLevelService;

    private final SkillService skillService;

    private final ProjectQuestionCsvService csvService;

    @GetMapping("/projects/{id}/questions")
    public String questions(
        @PathVariable Long id,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long projectRole,
        @RequestParam(required = false) Long skill,
        @RequestParam(required = false) Boolean enabled,
        @PageableDefault(page = DEFAULT_PAGE) Pageable pageable,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        var filter = QuestionFilter.builder()
            .search(search)
            .projectRole(projectRole)
            .skill(skill)
            .enabled(enabled)
            .build();
        var userId = currentUser.id();
        Pageable adjustedPageable = PageRequest.of(max(pageable.getPageNumber() - 1, 0),
            QUESTION_PAGE_SIZE, Sort.by(UUID).ascending());
        var questions = projectRole != null ?
            projectService.findQuestions(id, userId, filter, adjustedPageable) :
            Page.<ProjectQuestionLevelDto>empty(adjustedPageable);
        questionsPage(id, userId, filter, questions, adjustedPageable, model);
        return "page/project/questions";
    }

    @PostMapping("/projects/{id}/questions")
    public String saveQuestions(
        @PathVariable Long id,
        @RequestParam() Long projectRole,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long skill,
        @RequestParam(required = false) Boolean enabled,
        @RequestParam(defaultValue = "1") int page,
        @ModelAttribute(QUESTION_LEVELS) ProjectQuestionsForm questionLevels,
        @CurrentUserParam CurrentUser currentUser,
        RedirectAttributes redirectAttributes
    ) {
        projectService.saveQuestions(id, currentUser.id(), projectRole, questionLevels);
        var filter = QuestionFilter.builder()
            .search(search)
            .projectRole(projectRole)
            .skill(skill)
            .enabled(enabled)
            .build();
        redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
        return "redirect:/projects/%d/questions?page=%s%s".formatted(id, page, filter.buildExtraQuery());
    }

    @PostMapping("/projects/{id}/questions/upload")
    public String upload(@PathVariable Long id,
                         @RequestParam("file") MultipartFile file,
                         @CurrentUserParam CurrentUser currentUser,
                         RedirectAttributes redirectAttributes) {
        try {
            csvService.upload(id, currentUser.id(), file);
            redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(ERROR, ex.getMessage());
        }
        return "redirect:/projects/{id}/questions";
    }

    @GetMapping("/projects/{id}/questions/download")
    public void download(@PathVariable Long id,
                         @RequestParam(required = false) String search,
                         @RequestParam(required = false) Long projectRole,
                         @RequestParam(required = false) Long skill,
                         @RequestParam(required = false) Boolean enabled,
                         @CurrentUserParam CurrentUser currentUser,
                         HttpServletResponse response) throws Exception {
        var filter = QuestionFilter.builder()
            .search(search)
            .projectRole(projectRole)
            .skill(skill)
            .enabled(enabled)
            .build();
        var questions = csvService.download(id, currentUser.id(), filter);
        download(response, questions, CsvProjectQuestionDto.class, EXPORT_FILE_NAME);
    }

    private void questionsPage(Long id, Long userId, QuestionFilter filter, Page<ProjectQuestionLevelDto> questions,
                               Pageable pageable, Model model) {
        ProjectQuestionsForm form = ProjectQuestionsForm.builder().questions(questions.getContent()).build();
        model.addAttribute(PROJECT, projectService.findById(id, userId));
        model.addAttribute(QUESTION_LEVELS, form);
        model.addAttribute(QUESTION_PAGE, questions);
        model.addAttribute(CAREER_LEVELS, careerLevelService.findAllValues());
        model.addAttribute(PROJECT_ROLES, projectRoleService.findAllValues());
        model.addAttribute(SKILLS, skillService.findAllValues());
        model.addAttribute(FILTER, filter);
        model.addAttribute(QUESTION_PAGE_SOURCE, "/projects/%d/questions".formatted(id));
        model.addAttribute(CAN_EDIT, projectService.canEdit(id, userId));
        pageAttribute(model, pageable, questions, filter);
    }
}
