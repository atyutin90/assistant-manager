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
import ru.otus.dto.ManagerUserDto;
import ru.otus.dto.UserDto;
import ru.otus.services.CareerLevelService;
import ru.otus.services.JwtService;
import ru.otus.services.ManagerUserService;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.ProjectService;
import ru.otus.services.SkillService;
import ru.otus.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.CAREER_LEVELS;
import static ru.otus.controllers.pages.AbstractPageController.EXTRA_QUERY;
import static ru.otus.controllers.pages.AbstractPageController.FILTER;
import static ru.otus.controllers.pages.AbstractPageController.PROJECTS;
import static ru.otus.controllers.pages.AbstractPageController.PROJECT_ROLES;
import static ru.otus.controllers.pages.AbstractPageController.SKILLS;
import static ru.otus.controllers.pages.AbstractPageController.USERS;

@DisplayName("Контроллер сотрудников менеджера")
@WebMvcTest(ManagerUserPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "manager")
class ManagerUserPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ManagerUserService managerUserService;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectRoleService projectRoleService;

    @MockitoBean
    private CareerLevelService careerLevelService;

    @MockitoBean
    private SkillService skillService;

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
    @DisplayName("страница должна содержать сотрудников, фильтр и справочники")
    void shouldRenderEmployees() throws Exception {
        var users = List.of(ManagerUserDto.builder()
            .id(7L)
            .username("employee")
            .skillLevels(List.of())
            .build());
        when(managerUserService.findAll(org.mockito.ArgumentMatchers.any())).thenReturn(users);
        when(projectService.findAll(org.mockito.ArgumentMatchers.any(
            ru.otus.dto.filter.ProjectFilter.class))).thenReturn(List.of());

        mvc.perform(get("/employees")
                .with(user("manager"))
                .param("search", "Иван")
                .param("projectRole", "2")
                .param("careerLevel", "3")
                .param("skill", "4")
                .param("project", "6"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/employee/list"))
            .andExpect(model().attribute(USERS, users))
            .andExpect(model().attributeExists(FILTER, PROJECTS, PROJECT_ROLES, CAREER_LEVELS, SKILLS, EXTRA_QUERY));

        verify(managerUserService).findAll(argThat(filter ->
            filter.managerId().equals(5L) && filter.project().equals(6L)));
    }
}
