package ru.otus.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.dto.filter.UserFilter;
import ru.otus.services.CareerLevelService;
import ru.otus.services.JwtService;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.UserService;
import ru.otus.services.staffevaluation.StaffEvaluationService;
import ru.otus.services.staffevaluationuser.StaffEvaluationServiceUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.otus.entity.enums.UserRole.USER;

@DisplayName("Контроллер сотрудников оценки персонала")
@WebMvcTest(StaffEvaluationEmployeePageController.class)
@AutoConfigureMockMvc(addFilters = false)
class StaffEvaluationEmployeePageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private StaffEvaluationService staffEvaluationService;

    @MockitoBean
    private StaffEvaluationServiceUser staffEvaluationServiceUser;

    @MockitoBean
    private ProjectRoleService projectRoleService;

    @MockitoBean
    private CareerLevelService careerLevelService;

    @Test
    @DisplayName("назначенные сотрудники должны удаляться по фильтру")
    void shouldRemoveEmployeesByFilter() throws Exception {
        mvc.perform(delete("/staff-evaluations/5/employees")
                .param("search", "Иванов")
                .param("project_role", "7"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/staff-evaluations/5/employees"));

        var filterCaptor = ArgumentCaptor.forClass(UserFilter.class);
        verify(staffEvaluationService).removeEmployees(org.mockito.ArgumentMatchers.eq(5L), filterCaptor.capture());
        assertThat(filterCaptor.getValue().search()).isEqualTo("Иванов");
        assertThat(filterCaptor.getValue().projectRole()).isEqualTo(7L);
        assertThat(filterCaptor.getValue().role()).isEqualTo(USER);
    }
}
