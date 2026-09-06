package ru.otus.controllers.pages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ManagerEmployeeSearchPageController implements AbstractPageController {

    @GetMapping("/employees/ai-search")
    public String page() {
        return "page/employee/ai-search";
    }
}
