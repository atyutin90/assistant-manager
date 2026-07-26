package ru.otus.controllers.pages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.dto.ManagerAccessDto;
import ru.otus.dto.ProjectDto;
import ru.otus.dto.UserDto;
import ru.otus.services.JwtService;
import ru.otus.services.ProjectService;
import ru.otus.services.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.MANAGERS;
import static ru.otus.controllers.pages.AbstractPageController.PROJECT;
import static ru.otus.controllers.pages.AbstractPageController.SUCCESS_OPERATION;

@DisplayName("Контроллер доступа к проекту")
@WebMvcTest(ProjectAccessPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "manager")
class ProjectAccessPageControllerTest {

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
    @DisplayName("страница должна содержать проект и доступных менеджеров")
    void shouldRenderProjectAccess() throws Exception {
        var project = ProjectDto.builder().id(3L).name("Project").active(true).ownerId(5L).build();
        var managers = List.of(ManagerAccessDto.builder().id(8L).username("manager2").build());
        when(projectService.findByIdAndOwnerId(3L, 5L)).thenReturn(project);
        when(projectService.findManagerAccessOptions(3L, 5L)).thenReturn(managers);

        mvc.perform(get("/projects/3/access").with(user("manager")))
            .andExpect(status().isOk())
            .andExpect(view().name("page/project/access"))
            .andExpect(model().attribute(PROJECT, project))
            .andExpect(model().attribute(MANAGERS, managers));
    }

    @Test
    @DisplayName("список доступа должен сохраняться")
    void shouldSaveProjectAccess() throws Exception {
        mvc.perform(post("/projects/3/access")
                .with(user("manager"))
                .param("managerIds", "8", "9"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/projects/3/access"))
            .andExpect(flash().attribute(SUCCESS_OPERATION, true));

        verify(projectService).saveAccess(3L, 5L, Set.of(8L, 9L));
    }
}
