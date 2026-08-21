package ru.otus.controllers.pages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.dto.ProjectDto;
import ru.otus.dto.ProjectManagerDto;
import ru.otus.dto.UserDto;
import ru.otus.dto.filter.ProjectFilter;
import ru.otus.services.JwtService;
import ru.otus.services.project.ProjectService;
import ru.otus.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.FILTER;
import static ru.otus.controllers.pages.AbstractPageController.IS_EDIT;
import static ru.otus.controllers.pages.AbstractPageController.PROJECT;
import static ru.otus.controllers.pages.AbstractPageController.PROJECTS;
import static ru.otus.controllers.pages.AbstractPageController.SUCCESS_COPY_OPERATION;
import static ru.otus.controllers.pages.AbstractPageController.SUCCESS_DELETE_OPERATION;
import static ru.otus.controllers.pages.AbstractPageController.SUCCESS_OPERATION;

@DisplayName("Контроллер проектов")
@WebMvcTest(ProjectPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "manager")
class ProjectPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUpCurrentUser() {
        when(userService.findByUsername("manager"))
            .thenReturn(Optional.of(UserDto.builder().id(5L).username("manager").build()));
    }

    @Test
    @DisplayName("страница должна содержать проекты менеджера")
    void shouldRenderProjects() throws Exception {
        var page = new PageImpl<ProjectDto>(List.of());
        when(projectService.findAll(any(ProjectFilter.class), any())).thenReturn(page);

        mvc.perform(get("/projects").with(user("manager")).param("search", "CRM"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/project/list"))
            .andExpect(model().attribute(PROJECTS, page))
            .andExpect(model().attributeExists(FILTER));
    }

    @Test
    @DisplayName("форма нового проекта должна отображаться")
    void shouldRenderNewProjectForm() throws Exception {
        mvc.perform(get("/projects/new").with(user("manager")))
            .andExpect(status().isOk())
            .andExpect(view().name("page/project/form"))
            .andExpect(model().attribute(IS_EDIT, false))
            .andExpect(model().attributeExists(PROJECT));
    }

    @Test
    @DisplayName("форма существующего проекта должна отображаться")
    void shouldRenderProjectForm() throws Exception {
        var project = project(3L);
        when(projectService.findById(3L, 5L)).thenReturn(project);

        mvc.perform(get("/projects/3").with(user("manager")))
            .andExpect(status().isOk())
            .andExpect(view().name("page/project/form"))
            .andExpect(model().attribute(PROJECT, project))
            .andExpect(model().attribute(IS_EDIT, true))
            .andExpect(model().attribute("canManageAccess", true));
    }

    @Test
    @DisplayName("редактор проекта должен иметь возможность управлять доступом, но не удалять проект")
    void shouldAllowEditorToManageAccessWithoutDelete() throws Exception {
        var project = ProjectDto.builder()
            .id(3L)
            .name("CRM")
            .active(true)
            .owner(ProjectManagerDto.builder().id(8L).username("owner").build())
            .build();
        when(projectService.findById(3L, 5L)).thenReturn(project);
        when(projectService.canEdit(3L, 5L)).thenReturn(true);

        mvc.perform(get("/projects/3").with(user("manager")))
            .andExpect(status().isOk())
            .andExpect(model().attribute("canManageAccess", true))
            .andExpect(model().attribute("canDelete", false));
    }

    @Test
    @DisplayName("валидный проект должен сохраняться")
    void shouldSaveProject() throws Exception {
        when(projectService.save(any(ProjectDto.class), org.mockito.ArgumentMatchers.eq(5L)))
            .thenReturn(project(3L));

        mvc.perform(post("/projects")
                .with(user("manager"))
                .param("name", "CRM")
                .param("description", "CRM project")
                .param("active", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/projects/3"))
            .andExpect(flash().attribute(SUCCESS_OPERATION, true));

        verify(projectService).save(any(ProjectDto.class), org.mockito.ArgumentMatchers.eq(5L));
    }

    @Test
    @DisplayName("невалидный проект должен возвращать форму")
    void shouldRejectInvalidProject() throws Exception {
        mvc.perform(post("/projects")
                .with(user("manager"))
                .param("name", "")
                .param("active", "true"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/project/form"))
            .andExpect(model().attributeHasErrors(PROJECT));

        verify(projectService, never()).save(any(), any());
    }

    @Test
    @DisplayName("проект должен копироваться")
    void shouldCopyProject() throws Exception {
        mvc.perform(post("/projects/3").with(user("manager")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/projects"))
            .andExpect(flash().attribute(SUCCESS_COPY_OPERATION, true));

        verify(projectService).copy(3L, 5L);
    }

    @Test
    @DisplayName("проект должен удаляться")
    void shouldDeleteProject() throws Exception {
        mvc.perform(delete("/projects/3").with(user("manager")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/projects"))
            .andExpect(flash().attribute(SUCCESS_DELETE_OPERATION, true));

        verify(projectService).deleteById(3L, 5L);
    }

    private ProjectDto project(Long id) {
        return ProjectDto.builder()
            .id(id)
            .name("CRM")
            .description("CRM project")
            .active(true)
            .owner(ProjectManagerDto.builder().id(5L).username("manager").build())
            .build();
    }
}
