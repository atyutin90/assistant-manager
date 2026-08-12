package ru.otus.controllers.pages;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import ru.otus.dto.page.PageDataFilter;

import java.util.Map;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;


public interface AbstractPageController {

    int DEFAULT_PAGE = 1;

    String ID = "id";

    String UUID ="uuid";

    String CURRENT_PAGE = "currentPage";

    String TOTAL_PAGES = "totalPages";

    String PAGE_SIZE = "pageSize";

    String SORT_FIELD = "sortField";

    String SORT_DIRECTION = "sortDirection";

    String REVERSE_SORT_DIRECTION = "reverseSortDirection";

    String EXTRA_QUERY = "extraQuery";

    String FILTER = "filter";

    String IS_EDIT = "isEdit";

    String USER_ROLES = "userRoles";

    String TEAM_LEADS = "teamLeads";

    String CAREER_LEVELS = "careerLevels";

    String CAREER_LEVEL = "careerLevel";

    String PROJECT_ROLES = "projectRoles";

    String PROJECT_ROLE = "projectRole";

    String SKILLS = "skills";

    String SKILL = "skill";

    String QUESTIONS = "questions";

    String QUESTION = "question";

    String EMPLOYEES = "employees";

    String STAFF_EVALUATIONS = "staffEvaluations";

    String STAFF_EVALUATION = "staffEvaluation";

    String USERS = "users";

    String USER = "user";

    String ERROR = "error";

    String SUCCESS_OPERATION = "successOperation";

    String SUCCESS_COPY_OPERATION = "successCopyOperation";

    String SUCCESS_CHANGE_PASSWORD = "successChangePassword";

    String SUCCESS_DELETE_OPERATION = "successDeleteOperation";

    String PROJECT = "project";

    String PROJECTS = "projects";

    String MANAGERS = "managers";

    String PASSWORD_CHANGE = "passwordChange";

    String SHOW_PASSWORD_MODAL = "showPasswordModal";

    String FINISH = "finish";

    String SOURCE = "source";

    default <T> void pageAttribute(Model model, Pageable pageable, Page<T> page, PageDataFilter filter) {
        pageAttribute(model, pageable, page);
        model.addAttribute(EXTRA_QUERY, filter.buildExtraQuery());
    }

    default <T> void pageAttribute(Model model, Pageable pageable, Page<T> page) {
        var order = pageable.getSort().stream().findFirst().orElse(Sort.Order.asc(ID));
        model.addAttribute(SORT_FIELD, order.getProperty());
        model.addAttribute(SORT_DIRECTION, order.getDirection().name().toLowerCase());
        model.addAttribute(REVERSE_SORT_DIRECTION, order.isAscending() ? DESC.name() : ASC.name());
        model.addAttribute(CURRENT_PAGE, page.getNumber() + 1);
        model.addAttribute(TOTAL_PAGES, page.getTotalPages());
        model.addAttribute(PAGE_SIZE, page.getSize());
    }

    default void rejectFields(BindingResult bindingResult, Map<String, String> errors) {
        errors.forEach((field, message) -> bindingResult.rejectValue(field, "invalid", message));
    }

    default Pageable pageableOf(Pageable pageable) {
        return pageable.withPage(pageable.getPageNumber() - 1);
    }
}
