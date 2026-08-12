package ru.otus.controllers.pages;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

public interface StaffEvaluationController extends AbstractPageController {

    String MODAL = "modal";

    String SEARCH = "search";

    String CURRENT_PAGE = "CurrentPage";

    String TOTAL = "Total";

    String TOTAL_PAGES = "TotalPages";

    String PAGE_SIZE = "PageSize";

    String SORT_FIELD = "SortField";

    String SORT_DIRECTION = "SortDirection";

    String REVERSE_SORT_DIRECTION = "ReverseSortDirection";

    default void addPageAttributes(Model model, String prefix, Pageable pageable, Page<?> page) {
        var order = pageable.getSort().stream().findFirst().orElse(Sort.Order.asc(ID));

        model.addAttribute(prefix + TOTAL, page.getTotalElements());
        model.addAttribute(prefix + CURRENT_PAGE, page.getNumber() + 1);
        model.addAttribute(prefix + TOTAL_PAGES, page.getTotalPages());
        model.addAttribute(prefix + PAGE_SIZE, page.getSize());
        model.addAttribute(prefix + SORT_FIELD, order.getProperty());
        model.addAttribute(prefix + SORT_DIRECTION, order.getDirection().name().toLowerCase());
        model.addAttribute(prefix + REVERSE_SORT_DIRECTION, order.isAscending() ? ASC.name() : DESC.name());
    }
}
