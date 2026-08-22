package ru.otus.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomePageController implements AbstractPageController {

    @GetMapping("/")
    public String home() {
        return "page/home/edit";
    }
}
