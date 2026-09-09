package ru.otus.controllers.pages;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static java.lang.Boolean.TRUE;
import static ru.otus.controllers.handlers.ManagerPageModelAdvice.AI_SEARCH_AVAILABLE;

@Controller
public class ManagerEmployeeSearchPageController implements AbstractPageController {

    @GetMapping("/employees/search")
    public String page(Model model) {
        return TRUE.equals(model.getAttribute(AI_SEARCH_AVAILABLE))
            ? "page/employee/search"
            : "redirect:/settings/ai";
    }
}
