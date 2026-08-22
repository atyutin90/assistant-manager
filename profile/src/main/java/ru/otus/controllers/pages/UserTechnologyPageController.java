package ru.otus.controllers.pages;

import lombok.RequiredArgsConstructor;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.dto.filter.TechnologyFilter;
import ru.otus.dto.filter.UserTechnologyFilter;
import ru.otus.entity.enums.TechnologyLevel;
import ru.otus.services.TechnologyService;
import ru.otus.services.UserTechnologyService;
import ru.otus.services.ValueListService;

import java.util.Set;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class UserTechnologyPageController implements UserTechnologyController {

    private static final String USER_TECHNOLOGIES_EXTRA_QUERY = "userExtraQuery";

    private static final String TECHNOLOGIES_EXTRA_QUERY = "technologyExtraQuery";

    private static final String TECHNOLOGIES_SEARCH = "technologySearch";

    private static final String USER_TECHNOLOGY_PAGE = "user";

    private static final String TECHNOLOGY_PAGE = "technology";

    private static final String USER_TECHNOLOGIES = "userTechnologies";

    private static final String TECHNOLOGY_LEVELS = "technologyLevels";

    private final UserTechnologyService userTechnologyService;

    private final TechnologyService technologyService;

    private final ValueListService valueListService;

    @GetMapping("/technologies")
    public String list(@CurrentUserParam CurrentUser currentUser,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String technologySearch,
                       @RequestParam(required = false, defaultValue = "false") boolean modal,
                       @Qualifier(USER_TECHNOLOGY_PAGE)
                       @PageableDefault(page = DEFAULT_PAGE, sort = "technology.name", direction = ASC)
                       Pageable userTechnoloyPageable,
                       @Qualifier(TECHNOLOGY_PAGE)
                       @PageableDefault(page = DEFAULT_PAGE, sort = "name", direction = ASC)
                       Pageable technoloyPageable,
                       Model model) {

        var technologyFilter = TechnologyFilter.builder()
            .search(technologySearch)
            .enabled(true)
            .build();

        var userTechnologyFilter = UserTechnologyFilter.builder()
            .search(search)
            .userId(currentUser.id())
            .build();

        var technologies = technologyService.findAll(technologyFilter, pageableOf(technoloyPageable));
        var userTechnologies = userTechnologyService.findAll(userTechnologyFilter, pageableOf(userTechnoloyPageable));
        addPageAttributes(model, USER_TECHNOLOGY_PAGE, userTechnoloyPageable, userTechnologies);
        addPageAttributes(model, TECHNOLOGY_PAGE, technoloyPageable, technologies);
        model.addAttribute(TECHNOLOGIES, technologies);
        model.addAttribute(USER_TECHNOLOGIES, userTechnologies);
        model.addAttribute(TECHNOLOGY_LEVELS, valueListService.getValues("technology-level"));
        model.addAttribute(USER_TECHNOLOGIES_EXTRA_QUERY, buildUserTechnologyQuery(userTechnologyFilter));
        model.addAttribute(TECHNOLOGIES_EXTRA_QUERY, buildTechnologyQuery(technologyFilter));
        model.addAttribute(SOURCE, "/technologies");
        model.addAttribute(MODAL, modal);
        return "page/technology/list";
    }

    @PostMapping("/technologies/selected")
    public String add(@RequestParam(required = false) Set<Long> technologyIds,
                      @CurrentUserParam CurrentUser currentUser,
                      RedirectAttributes redirectAttributes) {
        userTechnologyService.addSelected(currentUser.id(), technologyIds);
        redirectAttributes.addFlashAttribute("technologySaved", true);
        return "redirect:/technologies";
    }

    @PostMapping("/technologies/{id}/level")
    public String changeLevel(@PathVariable Long id,
                              @RequestParam TechnologyLevel level,
                              @CurrentUserParam CurrentUser currentUser,
                              RedirectAttributes redirectAttributes) {
        userTechnologyService.changeLevel(currentUser.id(), id, level);
        redirectAttributes.addFlashAttribute("technologyLevelSaved", true);
        return "redirect:/technologies";
    }

    @DeleteMapping("/technologies/{id}/delete")
    public String delete(@PathVariable Long id,
                         @CurrentUserParam CurrentUser currentUser,
                         RedirectAttributes redirectAttributes) {
        userTechnologyService.delete(currentUser.id(), id);
        redirectAttributes.addFlashAttribute("technologyDeleted", true);
        return "redirect:/technologies";
    }

    private String buildUserTechnologyQuery(UserTechnologyFilter filter) {
        StringBuilder query = new StringBuilder();
        filter.appendQueryParam(query, SEARCH, filter.search());
        return query.toString();
    }

    private String buildTechnologyQuery(TechnologyFilter filter) {
        StringBuilder query = new StringBuilder();
        filter.appendQueryParam(query, MODAL, true);
        filter.appendQueryParam(query, TECHNOLOGIES_SEARCH, filter.search());
        return query.toString();
    }
}
