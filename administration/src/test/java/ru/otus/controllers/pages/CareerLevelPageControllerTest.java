package ru.otus.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.dto.CareerLevelDto;
import ru.otus.services.CareerLevelService;
import ru.otus.services.JwtService;
import ru.otus.services.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.CAREER_LEVEL;
import static ru.otus.controllers.pages.AbstractPageController.CAREER_LEVELS;
import static ru.otus.controllers.pages.AbstractPageController.CURRENT_PAGE;
import static ru.otus.controllers.pages.AbstractPageController.ID;
import static ru.otus.controllers.pages.AbstractPageController.IS_EDIT;
import static ru.otus.controllers.pages.AbstractPageController.SORT_FIELD;
import static ru.otus.controllers.pages.AbstractPageController.SUCCESS_OPERATION;

@DisplayName("Контроллер для карьерных уровней")
@WebMvcTest(CareerLevelPageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CareerLevelPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CareerLevelService careerLevelService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @DisplayName("страница списка карьерных уровней должна содержать уровни и параметры пагинации")
    @Test
    void shouldRenderCareerLevelList() throws Exception {
        var values = List.of(careerLevelOf(1L));
        var page = new PageImpl<>(values);
        when(careerLevelService.findAll(any())).thenReturn(page);

        mvc.perform(get("/career-levels"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/manual/career-level/list"))
            .andExpect(model().attribute(CAREER_LEVELS, page))
            .andExpect(model().attribute(CURRENT_PAGE, 1))
            .andExpect(model().attribute(SORT_FIELD, ID));
    }

    @DisplayName("форма нового карьерного уровня должна отображаться с пустым DTO")
    @Test
    void shouldRenderNewCareerLevelPage() throws Exception {
        mvc.perform(get("/career-levels/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/manual/career-level/form"))
            .andExpect(model().attribute(IS_EDIT, false))
            .andExpect(model().attributeExists(CAREER_LEVEL));
    }

    @DisplayName("форма существующего карьерного уровня должна отображаться с найденным DTO")
    @Test
    void shouldRenderCareerLevelEditPage() throws Exception {
        var value = careerLevelOf(1L);
        when(careerLevelService.findById(1L)).thenReturn(value);

        mvc.perform(get("/career-levels/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/manual/career-level/form"))
            .andExpect(model().attribute(CAREER_LEVEL, value))
            .andExpect(model().attribute(IS_EDIT, true));
        verify(careerLevelService).findById(1L);
    }

    @DisplayName("созданный карьерный уровень должен сохраняться и открываться")
    @Test
    void shouldCreateCareerLevelAndRedirect() throws Exception {
        when(careerLevelService.create(any(CareerLevelDto.class))).thenReturn(careerLevelOf(8L));

        mvc.perform(post("/career-levels")
                .param("enabled", "true")
                .param("code", "LEAD")
                .param("name", "Lead")
                .param("position", "8"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/career-levels/8"))
            .andExpect(flash().attribute(SUCCESS_OPERATION, true));
        verify(careerLevelService).create(any(CareerLevelDto.class));
    }

    @DisplayName("изменяемый карьерный уровень должен сохраняться и открываться")
    @Test
    void shouldChangeCareerLevelAndRedirect() throws Exception {
        when(careerLevelService.update(any(CareerLevelDto.class))).thenReturn(careerLevelOf(1L));

        mvc.perform(post("/career-levels")
                .param("id", "1")
                .param("enabled", "true")
                .param("code", "LEAD")
                .param("name", "Lead")
                .param("position", "8"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/career-levels/1"))
            .andExpect(flash().attribute(SUCCESS_OPERATION, true));
        verify(careerLevelService).update(any(CareerLevelDto.class));
    }

    @DisplayName("карьерный уровень должен удаляться")
    @Test
    void shouldDeleteCareerLevel() throws Exception {
        mvc.perform(delete("/career-levels/3"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/career-levels"));
        verify(careerLevelService).deleteById(3L);
    }

    private CareerLevelDto careerLevelOf(Long id) {
        return CareerLevelDto.builder()
            .id(id)
            .enabled(true)
            .code(id == 1L ? "INTERN" : "LEAD")
            .name(id == 1L ? "Intern" : "Lead")
            .position(id.intValue())
            .build();
    }
}
