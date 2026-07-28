package ru.otus.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.dto.filter.StaffEvaluationQuestionFilter;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.SkillService;
import ru.otus.services.question.QuestionService;
import ru.otus.services.staffevaluation.StaffEvaluationService;
import ru.otus.services.staffevaluationquestion.StaffEvaluationQuestionService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.FALSE;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class StaffEvaluationQuestionPageController implements StaffEvaluationController {

    private static final String ID_PREFIX = "id_";

    private static final String R_PROJECT_ROLE = "project_role";

    private static final String ASSIGNED_PAGE = "assigned";

    private static final String ASSIGNED_QUESTIONS = "assignedQuestions";

    private static final String ASSIGNED_QUESTION_FILTER = "assignedQuestionFilter";

    private static final String ASSIGNED_QUESTION_EXTRA_QUERY = "assignedQuestionExtraQuery";

    private static final String R_QUESTION_SEARCH = "question_search";

    private static final String R_QUESTION_PROJECT_ROLE = "question_project_role";

    private static final String R_QUESTION_SKILL = "question_skill";

    private static final String R_QUESTION_ENABLED = "question_enabled";

    private static final String QUESTION_PAGE = "question";

    private static final String QUESTION_FILTER = "questionFilter";

    private static final String QUESTION_EXTRA_QUERY = "questionExtraQuery";

    private final StaffEvaluationService staffEvaluationService;

    private final StaffEvaluationQuestionService staffEvaluationQuestionService;

    private final QuestionService questionService;

    private final ProjectRoleService projectRoleService;

    private final SkillService skillService;

    @GetMapping("/staff-evaluations/{id}/questions")
    public String questions(@PathVariable Long id,
                            @RequestParam(required = false, name = R_QUESTION_SEARCH) String questionSearch,
                            @RequestParam(required = false, name = R_QUESTION_PROJECT_ROLE) Long questionProjectRole,
                            @RequestParam(required = false, name = R_QUESTION_SKILL) Long questionSkill,
                            @RequestParam(required = false, name = R_QUESTION_ENABLED) Boolean questionEnabled,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false, name = R_PROJECT_ROLE) Long projectRole,
                            @RequestParam(required = false) Long skill,
                            @RequestParam(required = false, defaultValue = FALSE) boolean modal,
                            @Qualifier(ASSIGNED_PAGE)
                            @PageableDefault(page = DEFAULT_PAGE, sort = "position", direction = ASC)
                            Pageable assignedPageable,
                            @Qualifier(QUESTION_PAGE)
                            @PageableDefault(page = DEFAULT_PAGE, sort = ID, direction = ASC)
                            Pageable questionPageable,
                            Model model) {
        var questionFilter = QuestionFilter.builder()
            .search(questionSearch)
            .projectRole(questionProjectRole)
            .skill(questionSkill)
            .enabled(questionEnabled)
            .build();
        var assignedFilter = StaffEvaluationQuestionFilter.builder()
            .search(search)
            .projectRole(projectRole)
            .skill(skill)
            .staffEvaluationId(id)
            .build();

        var staffEvaluation = staffEvaluationService.findQuestionsById(id);
        var questions = questionService.findAll(questionFilter, pageableOf(questionPageable));
        var assignedQuestions = staffEvaluationQuestionService.findAll(assignedFilter, pageableOf(assignedPageable));
        addPageAttributes(model, ASSIGNED_PAGE, assignedPageable, assignedQuestions);
        addPageAttributes(model, QUESTION_PAGE, questionPageable, questions);
        model.addAttribute(PROJECT_ROLES, projectRoleService.findAllValues());
        model.addAttribute(SKILLS, skillService.findAllValues());
        model.addAttribute(ASSIGNED_QUESTION_EXTRA_QUERY, buildAssignedQuestionQuery(assignedFilter));
        model.addAttribute(QUESTION_EXTRA_QUERY, buildQuestionQuery(questionFilter));
        model.addAttribute(STAFF_EVALUATION, staffEvaluation);
        model.addAttribute(ASSIGNED_QUESTIONS, assignedQuestions);
        model.addAttribute(QUESTIONS, questions);
        model.addAttribute(QUESTION_FILTER, questionFilter);
        model.addAttribute(ASSIGNED_QUESTION_FILTER, assignedFilter);
        model.addAttribute(MODAL, modal);
        model.addAttribute(SOURCE, "/staff-evaluations/%d/questions".formatted(id));
        return "page/staff-evaluation/questions";
    }

    @PostMapping("/staff-evaluations/{id}/questions/selected")
    public String addSelectedQuestions(@PathVariable Long id,
                                       @RequestParam(required = false) Set<Long> questionIds) {
        if (isNotEmpty(questionIds)) {
            staffEvaluationService.addQuestions(id, questionIds);
        }
        return redirectToQuestions(id);
    }

    @PostMapping("/staff-evaluations/{id}/questions")
    public String addFilteredQuestions(@PathVariable Long id,
                                       @RequestParam(required = false, name = R_QUESTION_SEARCH) String search,
                                       @RequestParam(required = false, name = R_QUESTION_PROJECT_ROLE) Long projectRole,
                                       @RequestParam(required = false, name = R_QUESTION_SKILL) Long skill,
                                       @RequestParam(required = false, name = R_QUESTION_ENABLED) Boolean enabled) {
        var filter = QuestionFilter.builder()
            .search(search)
            .projectRole(projectRole)
            .skill(skill)
            .enabled(enabled)
            .build();
        staffEvaluationService.addQuestions(id, filter);
        return redirectToQuestions(id);
    }

    @PostMapping("/staff-evaluations/{id}/questions/positions")
    public String updateQuestionPositions(@PathVariable Long id,
                                          @RequestParam Map<String, String> parameters) {
        staffEvaluationService.updateQuestionPositions(id, extractQuestionPositions(parameters));
        return redirectToQuestions(id);
    }

    @DeleteMapping("/staff-evaluations/{id}/questions/selected")
    public String removeSelectedQuestions(@PathVariable Long id,
                                          @RequestParam(required = false) String search,
                                          @RequestParam(required = false, name = R_PROJECT_ROLE) Long projectRole,
                                          @RequestParam(required = false) Long skill,
                                          @RequestParam(required = false) Set<Long> questionIds) {
        var filter = StaffEvaluationQuestionFilter.builder()
            .search(search)
            .projectRole(projectRole)
            .skill(skill)
            .build();

        if (isNotEmpty(questionIds)) {
            staffEvaluationService.removeQuestions(id, questionIds);
        }
        return redirectToQuestions(id, filter);
    }

    private String redirectToQuestions(Long id) {
        return "redirect:/staff-evaluations/%d/questions".formatted(id);
    }

    private String redirectToQuestions(Long id, StaffEvaluationQuestionFilter filter) {
        var url = new StringBuilder(redirectToQuestions(id));
        var query = buildAssignedQuestionQuery(filter);
        if (StringUtils.isNotEmpty(query)) {
            url.append('?').append(query);
        }
        return url.toString();
    }

    private String buildAssignedQuestionQuery(StaffEvaluationQuestionFilter filter) {
        StringBuilder query = new StringBuilder();
        filter.appendQueryParam(query, SEARCH, filter.search());
        filter.appendQueryParam(query, PROJECT_ROLE, filter.projectRole());
        filter.appendQueryParam(query, SKILL, filter.skill());
        return query.toString();
    }

    private String buildQuestionQuery(QuestionFilter filter) {
        StringBuilder query = new StringBuilder();
        filter.appendQueryParam(query, R_QUESTION_SEARCH, filter.search());
        filter.appendQueryParam(query, R_QUESTION_PROJECT_ROLE, filter.projectRole());
        filter.appendQueryParam(query, R_QUESTION_SKILL, filter.skill());
        filter.appendQueryParam(query, R_QUESTION_ENABLED, filter.enabled());
        filter.appendQueryParam(query, MODAL, true);
        return query.toString();
    }

    private Map<Long, Integer> extractQuestionPositions(Map<String, String> parameters) {
        var positions = new HashMap<Long, Integer>();
        parameters.keySet().stream()
            .filter(key -> StringUtils.isNotEmpty(key) && key.startsWith(ID_PREFIX))
            .map(key -> Pair.of(toLong(key.substring(ID_PREFIX.length())), toInt(parameters.get(key))))
            .filter(pair -> pair.getValue() != null && pair.getKey() != null && pair.getValue() > 0)
            .forEach(pair ->  positions.put(pair.getKey(), pair.getValue()));
        return positions;
    }
}
