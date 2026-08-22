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
import ru.otus.dto.CsvUserDto;
import ru.otus.dto.CsvTechnologyDto;
import ru.otus.dto.TechnologyDto;
import ru.otus.dto.filter.TechnologyFilter;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.services.TechnologyService;
import ru.otus.services.csv.CsvService;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Controller
@RequiredArgsConstructor
public class TechnologyPageController implements DownloadPageController {

    private final TechnologyService technologyService;

    private final CsvService csvService;

    @GetMapping("/technologies")
    public String list(@RequestParam(required = false) String search,
                       @PageableDefault(page = DEFAULT_PAGE, sort = ID, direction = ASC) Pageable pageable,
                       Model model) {
        var filter = TechnologyFilter.builder().search(search).build();
        var technologies = technologyService.findAll(filter, pageableOf(pageable));
        pageAttribute(model, pageable, technologies, filter);
        model.addAttribute(TECHNOLOGIES, technologies);
        model.addAttribute(FILTER, filter);
        return "page/manual/technology/list";
    }

    @GetMapping("/technologies/new")
    public String create(Model model) {
        model.addAttribute(TECHNOLOGY, TechnologyDto.builder().enabled(true).build());
        model.addAttribute(IS_EDIT, false);
        return "page/manual/technology/form";
    }

    @GetMapping("/technologies/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute(TECHNOLOGY, technologyService.findById(id));
        model.addAttribute(IS_EDIT, true);
        return "page/manual/technology/form";
    }

    @PostMapping("/technologies")
    public String save(@Valid @ModelAttribute(TECHNOLOGY) TechnologyDto technology,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(IS_EDIT, technology.id() != null);
            return "page/manual/technology/form";
        }
        try {
            if (technology.id() != null) {
                technologyService.update(technology);
            } else {
                technology = technologyService.create(technology);
            }
            redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
            return "redirect:/technologies/%d".formatted(technology.id());
        } catch (NonUniqueValueException ex) {
            rejectFields(bindingResult, ex.getInfo());
            model.addAttribute(IS_EDIT, technology.id() != null);
            return "page/manual/technology/form";
        }
    }

    @DeleteMapping("/technologies/{id}")
    public String delete(@PathVariable long id) {
        technologyService.deleteById(id);
        return "redirect:/technologies";
    }

    @PostMapping("/technologies/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         RedirectAttributes redirectAttributes) {
        try {
            csvService.uploadTechnologies(file);
            redirectAttributes.addFlashAttribute(SUCCESS_OPERATION, true);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute(ERROR, ex.getMessage());
        }
        return "redirect:/technologies";
    }

    @GetMapping("/technologies/download")
    public void download(@RequestParam(required = false) String search,
                         HttpServletResponse response) throws Exception {
        var csvTechnologies = csvService.downloadTechnologies(TechnologyFilter.builder()
            .search(search)
            .build());
        download(response, csvTechnologies, CsvTechnologyDto.class, "technologies.csv");
    }
}
