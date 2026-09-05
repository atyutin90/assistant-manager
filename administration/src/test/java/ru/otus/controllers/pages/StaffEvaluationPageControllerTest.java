package ru.otus.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.dto.StaffEvaluationDto;
import ru.otus.dto.filter.StaffEvaluationFilter;
import ru.otus.services.JwtService;
import ru.otus.services.UserService;
import ru.otus.services.staffevaluation.StaffEvaluationService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.IS_EDIT;
import static ru.otus.controllers.pages.AbstractPageController.STAFF_EVALUATION;
import static ru.otus.controllers.pages.AbstractPageController.STAFF_EVALUATIONS;
import static ru.otus.entity.enums.StaffEvaluationStatus.DRAFT;

@DisplayName("Контроллер для оценка персонала ")
@WebMvcTest(StaffEvaluationPageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StaffEvaluationPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private StaffEvaluationService staffEvaluationService;

    @DisplayName("страница оценка персонала должна отображаться")
    @Test
    void shouldRenderStaffEvaluationList() throws Exception {
        var page = new PageImpl<StaffEvaluationDto>(List.of());
        when(staffEvaluationService.findAll(any(StaffEvaluationFilter.class), any()))
            .thenReturn(page);

        mvc.perform(get("/staff-evaluations"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/staff-evaluation/list"))
            .andExpect(model().attribute(STAFF_EVALUATIONS, page));
    }

    @DisplayName("форма нового оценка персонала должна отображаться с пустым DTO")
    @Test
    void shouldRenderNewStaffEvaluationPage() throws Exception {
        mvc.perform(get("/staff-evaluations/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/staff-evaluation/form"))
            .andExpect(model().attribute(IS_EDIT, false))
            .andExpect(model().attributeExists(STAFF_EVALUATION));
    }

    @DisplayName("оценка персонала должна создаваться")
    @Test
    void shouldCreateStaffEvaluation() throws Exception {
        when(staffEvaluationService.create(any(StaffEvaluationDto.class)))
            .thenReturn(staffEvaluationOf(2L));

        mvc.perform(post("/staff-evaluations")
                .param("name", "Оценка 2026")
                .param("dateFrom", "2026-01-01")
                .param("dateTo", "2026-12-31")
                .param("status", DRAFT.name()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/staff-evaluations/2"));
        verify(staffEvaluationService).create(any(StaffEvaluationDto.class));
    }

    @DisplayName("оценка персонала должна редактироваться")
    @Test
    void shouldUpdateStaffEvaluation() throws Exception {
        when(staffEvaluationService.update(any(StaffEvaluationDto.class)))
            .thenReturn(staffEvaluationOf(1L));

        mvc.perform(post("/staff-evaluations")
                .param("id", String.valueOf(1L))
                .param("name", "Оценка 2026")
                .param("dateFrom", "2026-01-01")
                .param("dateTo", "2026-12-31")
                .param("status", DRAFT.name()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/staff-evaluations/1"));
        verify(staffEvaluationService).update(any(StaffEvaluationDto.class));
    }

    @DisplayName("оценка персонала должна запускаться")
    @Test
    void shouldStartStaffEvaluation() throws Exception {
        mvc.perform(post("/staff-evaluations/2/start"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/staff-evaluations/2"));
        verify(staffEvaluationService).start(2L);
    }

    @DisplayName("оценка персонала должна удаляться")
    @Test
    void shouldDeleteStaffEvaluation() throws Exception {
        doNothing().when(staffEvaluationService).deleteById(3L);

        mvc.perform(delete("/staff-evaluations/3/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/staff-evaluations"));

        verify(staffEvaluationService).deleteById(3L);
    }

    @DisplayName("оценка персонала должна запускаться")
    @Test
    void shouldCompleteStaffEvaluation() throws Exception {
        mvc.perform(post("/staff-evaluations/2/complete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/staff-evaluations/2"));
        verify(staffEvaluationService).complete(2L);
    }

    private StaffEvaluationDto staffEvaluationOf(Long id) {
        return StaffEvaluationDto.builder()
            .id(id)
            .name("Оценка 2026")
            .dateFrom(LocalDate.of(2026, 1, 1))
            .dateTo(LocalDate.of(2026, 12, 31))
            .status(DRAFT)
            .build();
    }
}
