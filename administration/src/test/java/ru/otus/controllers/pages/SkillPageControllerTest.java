package ru.otus.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.dto.SkillDto;
import ru.otus.services.JwtService;
import ru.otus.services.SkillService;
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
import static ru.otus.controllers.pages.AbstractPageController.SKILL;
import static ru.otus.controllers.pages.AbstractPageController.SKILLS;
import static ru.otus.controllers.pages.AbstractPageController.SORT_FIELD;

@DisplayName("Контроллер для навыков ")
@WebMvcTest(SkillPageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SkillPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SkillService skillService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CsvService csvService;

    @DisplayName("страница списка навыков должна отображаться")
    @Test
    void shouldRenderSkillList() throws Exception {
        var page = new PageImpl<>(List.of(skillOf(1L)));
        when(skillService.findAll(any())).thenReturn(page);

        mvc.perform(get("/skills"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/manual/skill/list"))
            .andExpect(model().attribute(SKILLS, page))
            .andExpect(model().attribute(CURRENT_PAGE, 1))
            .andExpect(model().attribute(SORT_FIELD, ID));
    }

    @DisplayName("форма нового навыка должна отображаться с пустым DTO")
    @Test
    void shouldRenderNewCareerLevelPage() throws Exception {
        mvc.perform(get("/skills/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/manual/skill/form"))
            .andExpect(model().attribute(IS_EDIT, false))
            .andExpect(model().attributeExists(SKILL));
    }

    @DisplayName("навык должна создаваться")
    @Test
    void shouldCreateSkill() throws Exception {
        when(skillService.create(any(SkillDto.class))).thenReturn(skillOf(7L));

        mvc.perform(post("/skills")
                .param("enabled", "true")
                .param("code", "HARD")
                .param("name", "Технические навыки")
                .param("position", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/skills/7"));
        verify(skillService).create(any(SkillDto.class));
    }

    @DisplayName("навык должен обновляться")
    @Test
    void shouldUpdateSkill() throws Exception {
        mvc.perform(post("/skills")
                .param("id", "1")
                .param("enabled", "true")
                .param("code", "HARD")
                .param("name", "Технические навыки")
                .param("position", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/skills/1"));
        verify(skillService).update(skillOf(1L));
    }

    @DisplayName("навык должна удаляться")
    @Test
    void shouldDeleteSkill() throws Exception {
        mvc.perform(delete("/skills/3"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/skills"));
        verify(skillService).deleteById(3L);
    }

    private SkillDto skillOf(Long id) {
        return SkillDto.builder()
            .id(id)
            .enabled(true)
            .code("HARD")
            .name("Технические навыки")
            .position(1)
            .build();
    }
}
