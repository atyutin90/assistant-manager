package ru.otus.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.dto.TechnologyDto;
import ru.otus.dto.filter.TechnologyFilter;
import ru.otus.services.JwtService;
import ru.otus.services.TechnologyService;
import ru.otus.services.UserService;
import ru.otus.services.csv.CsvService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.CURRENT_PAGE;
import static ru.otus.controllers.pages.AbstractPageController.ID;
import static ru.otus.controllers.pages.AbstractPageController.IS_EDIT;
import static ru.otus.controllers.pages.AbstractPageController.SORT_FIELD;
import static ru.otus.controllers.pages.AbstractPageController.TECHNOLOGIES;
import static ru.otus.controllers.pages.AbstractPageController.TECHNOLOGY;

@DisplayName("Контроллер для технологий ")
@WebMvcTest(TechnologyPageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TechnologyPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TechnologyService technologyService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CsvService csvService;

    @DisplayName("страница списка технологий должна отображаться")
    @Test
    void shouldRenderTechnologyList() throws Exception {
        var page = new PageImpl<>(List.of(technologyOf(1L)));
        when(technologyService.findAll(any(TechnologyFilter.class), any())).thenReturn(page);

        mvc.perform(get("/technologies"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/manual/technology/list"))
            .andExpect(model().attribute(TECHNOLOGIES, page))
            .andExpect(model().attribute(CURRENT_PAGE, 1))
            .andExpect(model().attribute(SORT_FIELD, ID));
    }

    @DisplayName("форма новой технологии должна отображаться с пустым DTO")
    @Test
    void shouldRenderNewTechnologyPage() throws Exception {
        mvc.perform(get("/technologies/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/manual/technology/form"))
            .andExpect(model().attribute(IS_EDIT, false))
            .andExpect(model().attributeExists(TECHNOLOGY));
    }

    @DisplayName("технология должна создаваться")
    @Test
    void shouldCreateTechnology() throws Exception {
        when(technologyService.create(any(TechnologyDto.class))).thenReturn(technologyOf(2L));

        mvc.perform(post("/technologies")
                .param("enabled", "true")
                .param("name", "Java"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/technologies/2"));
        verify(technologyService).create(any(TechnologyDto.class));
    }

    @DisplayName("технология должен обновляться")
    @Test
    void shouldUpdateTechnology() throws Exception {
        mvc.perform(post("/technologies")
                .param("id", "2")
                .param("enabled", "true")
                .param("name", "Spring"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/technologies/2"));
        verify(technologyService).update(technologyOf(2L));
    }

    @DisplayName("технология должна удаляться")
    @Test
    void shouldDeleteTechnology() throws Exception {
        mvc.perform(delete("/technologies/3"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/technologies"));
        verify(technologyService).deleteById(3L);
    }

    private TechnologyDto technologyOf(Long id) {
        return TechnologyDto.builder()
            .id(id)
            .enabled(true)
            .name("Spring")
            .build();
    }
}
