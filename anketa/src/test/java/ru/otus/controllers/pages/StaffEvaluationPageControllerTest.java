package ru.otus.controllers.pages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.dto.AssignedStaffEvaluationDto;
import ru.otus.dto.StaffEvaluationResultDto;
import ru.otus.dto.UserDto;
import ru.otus.services.JwtService;
import ru.otus.services.StaffEvaluationUserService;
import ru.otus.services.UserService;

import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.CURRENT_PAGE;
import static ru.otus.controllers.pages.AbstractPageController.STAFF_EVALUATIONS;
import static ru.otus.controllers.pages.StaffEvaluationPageControllerTest.EMPLOYEE;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.COMPLETED;

@DisplayName("Контроллер назначенных оценок")
@WebMvcTest(StaffEvaluationPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = EMPLOYEE)
class StaffEvaluationPageControllerTest {

    static final String EMPLOYEE = "employee";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StaffEvaluationUserService staffEvaluationUserService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUpCurrentUser() {
        when(userService.findByUsername(EMPLOYEE))
            .thenReturn(Optional.of(UserDto.builder().id(7L).username(EMPLOYEE).build()));
    }

    @Test
    @DisplayName("список должен содержать назначенные оценки и пагинацию")
    void shouldRenderAssignedEvaluations() throws Exception {
        var page = new PageImpl<AssignedStaffEvaluationDto>(List.of());
        when(staffEvaluationUserService.findAssigned(any(), any())).thenReturn(page);

        mvc.perform(get("/staff-evaluations").with(user(EMPLOYEE)))
            .andExpect(status().isOk())
            .andExpect(view().name("page/staff-evaluations/list"))
            .andExpect(model().attribute(STAFF_EVALUATIONS, page))
            .andExpect(model().attribute(CURRENT_PAGE, 1));

        verify(staffEvaluationUserService).findAssigned(any(), any());
    }

    @Test
    @DisplayName("страница результата должна содержать результат оценки")
    void shouldRenderEvaluationResult() throws Exception {
        var result = StaffEvaluationResultDto.builder()
            .assignmentId(12L)
            .name("Оценка")
            .dateFrom(of(2026, 1, 1))
            .dateTo(of(2026, 1, 31))
            .status(COMPLETED)
            .questions(List.of())
            .build();
        when(staffEvaluationUserService.findResult(7L, 3L)).thenReturn(result);

        mvc.perform(get("/staff-evaluations/3/detail").with(user(EMPLOYEE)))
            .andExpect(status().isOk())
            .andExpect(view().name("page/staff-evaluations/detail"))
            .andExpect(model().attribute("result", result));

        verify(staffEvaluationUserService).findResult(7L, 3L);
    }
}
