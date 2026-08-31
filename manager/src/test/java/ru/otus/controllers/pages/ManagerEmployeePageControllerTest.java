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
import ru.otus.dto.ManagerEmployeeDto;
import ru.otus.dto.ManagerEmployeeDetailsDto;
import ru.otus.dto.ManagerEmployeeAnswerDto;
import ru.otus.dto.ManagerCareerLevelAnswersDto;
import ru.otus.dto.ManagerSkillAnswersDto;
import ru.otus.dto.ManagerStaffEvaluationDto;
import ru.otus.dto.ProjectDto;
import ru.otus.dto.UserDto;
import ru.otus.entity.enums.AnswerResponse;
import ru.otus.services.CareerLevelService;
import ru.otus.services.JwtService;
import ru.otus.services.employee.ManagerEmployeeService;
import ru.otus.services.staffevaluation.ManagerStaffEvaluationService;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.project.ProjectService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.CAREER_LEVELS;
import static ru.otus.controllers.pages.AbstractPageController.EXTRA_QUERY;
import static ru.otus.controllers.pages.AbstractPageController.FILTER;
import static ru.otus.controllers.pages.AbstractPageController.PROJECTS;
import static ru.otus.controllers.pages.AbstractPageController.PROJECT_ROLES;
import static ru.otus.controllers.pages.AbstractPageController.SKILLS;
import static ru.otus.controllers.pages.AbstractPageController.STAFF_EVALUATIONS;
import static ru.otus.controllers.pages.AbstractPageController.USERS;

@DisplayName("Контроллер сотрудников менеджера")
@WebMvcTest(ManagerEmployeePageController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "manager")
class ManagerEmployeePageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ManagerEmployeeService managerEmployeeService;

    @MockitoBean
    private ManagerStaffEvaluationService managerStaffEvaluationService;

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
        var users = List.of(ManagerEmployeeDto.builder()
            .id(7L)
            .username("employee")
            .skillLevels(List.of())
            .build());
        when(managerEmployeeService.findAll(org.mockito.ArgumentMatchers.any())).thenReturn(users);
        var staffEvaluations = List.of(ManagerStaffEvaluationDto.builder().id(8L).name("2026").build());
        when(managerStaffEvaluationService.findAll()).thenReturn(staffEvaluations);
        when(projectService.findAll(org.mockito.ArgumentMatchers.any(
            ru.otus.dto.filter.ProjectFilter.class))).thenReturn(List.of());

        mvc.perform(get("/employees")
                .with(user("manager"))
                .param("search", "Иван")
                .param("projectRole", "2")
                .param("careerLevel", "3")
                .param("skill", "4")
                .param("project", "6")
                .param("staffEvaluation", "8"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/employee/list"))
            .andExpect(model().attribute(USERS, users))
            .andExpect(model().attribute(STAFF_EVALUATIONS, staffEvaluations))
            .andExpect(model().attributeExists(FILTER, PROJECTS, PROJECT_ROLES, CAREER_LEVELS, SKILLS, EXTRA_QUERY));

        verify(managerEmployeeService).findAll(argThat(filter ->
            filter.managerId().equals(5L)
                && filter.project().equals(6L)
                && filter.staffEvaluation().equals(8L)));
    }

    @Test
    @DisplayName("страница деталей должна содержать результаты сотрудника")
    void shouldRenderEmployeeDetails() throws Exception {
        var details = ManagerEmployeeDetailsDto.builder()
            .employeeId(7L)
            .fullName("Иванов Иван")
            .username("employee")
            .projectId(6L)
            .staffEvaluationId(8L)
            .skills(List.of(ManagerSkillAnswersDto.builder()
                .skillId(9L)
                .skill("Java")
                .calculatedCareerLevel("Junior")
                .careerLevels(List.of(ManagerCareerLevelAnswersDto.builder()
                    .careerLevelId(10L)
                    .careerLevel("Junior")
                    .answers(List.of(ManagerEmployeeAnswerDto.builder()
                        .question("Что такое JVM?")
                        .areaKnowledge("Java Core")
                        .section("JVM")
                        .response(AnswerResponse.YES)
                        .verifiedResponse(AnswerResponse.NO)
                        .build()))
                    .build()))
                .build()))
            .build();
        var projects = List.of(ProjectDto.builder().id(6L).name("Backend").build());
        var evaluations = List.of(ManagerStaffEvaluationDto.builder()
            .id(8L).name("Оценка 2026").build());
        when(managerEmployeeService.findDetails(7L, 8L, 6L, 5L, 1L)).thenReturn(details);
        when(projectService.findAll(org.mockito.ArgumentMatchers.any(
            ru.otus.dto.filter.ProjectFilter.class))).thenReturn(projects);
        when(managerStaffEvaluationService.findAllByEmployeeIdAndProjectRoleId(7L, 1L)).thenReturn(evaluations);

        mvc.perform(get("/employees/7/1/detail")
                .with(user("manager"))
                .param("staffEvaluation", "8")
                .param("project", "6"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/employee/detail"))
            .andExpect(model().attribute("employeeDetails", details))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("accordion")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Java")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Junior")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Что такое JVM?")));

        verify(managerEmployeeService).findDetails(7L, 8L, 6L, 5L, 1L);
    }
}
