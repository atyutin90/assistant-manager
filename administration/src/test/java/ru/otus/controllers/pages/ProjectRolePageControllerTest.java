package ru.otus.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.dto.ProjectRoleDto;
import ru.otus.services.JwtService;
import ru.otus.services.ProjectRoleService;
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
import static ru.otus.controllers.pages.AbstractPageController.CURRENT_PAGE;
import static ru.otus.controllers.pages.AbstractPageController.ID;
import static ru.otus.controllers.pages.AbstractPageController.IS_EDIT;
import static ru.otus.controllers.pages.AbstractPageController.PROJECT_ROLE;
import static ru.otus.controllers.pages.AbstractPageController.PROJECT_ROLES;
import static ru.otus.controllers.pages.AbstractPageController.SORT_FIELD;
import static ru.otus.controllers.pages.AbstractPageController.SUCCESS_OPERATION;

@DisplayName("Контроллер для проектных ролей ")
@WebMvcTest(ProjectRolePageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProjectRolePageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProjectRoleService projectRoleService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @DisplayName("страница списка проектных ролей должна отображаться")
    @Test
    void shouldRenderProjectRoleList() throws Exception {
        var page = new PageImpl<>(List.of(projectRoleOf(1L)));
        when(projectRoleService.findAll(any())).thenReturn(page);

        mvc.perform(get("/project-roles"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/manual/project-role/list"))
            .andExpect(model().attribute(PROJECT_ROLES, page))
            .andExpect(model().attribute(CURRENT_PAGE, 1))
            .andExpect(model().attribute(SORT_FIELD, ID));
    }

    @DisplayName("форма новой проектной роли должна отображаться с пустым DTO")
    @Test
    void shouldRenderNewCareerLevelPage() throws Exception {
        mvc.perform(get("/project-roles/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/manual/project-role/form"))
            .andExpect(model().attribute(IS_EDIT, false))
            .andExpect(model().attributeExists(PROJECT_ROLE));
    }

    @DisplayName("проектная роль должна создаваться")
    @Test
    void shouldCreateProjectRole() throws Exception {
        when(projectRoleService.create(any(ProjectRoleDto.class))).thenReturn(projectRoleOf(7L));

        mvc.perform(post("/project-roles")
                .param("enabled", "true")
                .param("code", "ARCHITECT")
                .param("name", "Architect")
                .param("position", "7"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/project-roles/7"));
        verify(projectRoleService).create(any(ProjectRoleDto.class));
    }

    @DisplayName("изменяемая проектная роль должен сохраняться и открываться")
    @Test
    void shouldChangeProjectRoleAndRedirect() throws Exception {
        when(projectRoleService.update(any(ProjectRoleDto.class))).thenReturn(projectRoleOf(1L));

        mvc.perform(post("/project-roles")
                .param("id", "1")
                .param("enabled", "true")
                .param("code", "ARCHITECT")
                .param("name", "Architect")
                .param("position", "7"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/project-roles/1"))
            .andExpect(flash().attribute(SUCCESS_OPERATION, true));
        verify(projectRoleService).update(any(ProjectRoleDto.class));
    }

    @DisplayName("проектная роль должна удаляться")
    @Test
    void shouldDeleteProjectRole() throws Exception {
        mvc.perform(delete("/project-roles/3"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/project-roles"));
        verify(projectRoleService).deleteById(3L);
    }

    private ProjectRoleDto projectRoleOf(Long id) {
        return ProjectRoleDto.builder()
            .id(id)
            .enabled(true)
            .code(id == 1L ? "ANALYST" : "ARCHITECT")
            .name(id == 1L ? "Аналитик" : "Architect")
            .position(id.intValue())
            .build();
    }
}
