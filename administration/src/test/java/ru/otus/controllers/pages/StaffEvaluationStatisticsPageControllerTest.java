package ru.otus.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.dto.StaffEvaluationDto;
import ru.otus.dto.StaffEvaluationUserStatisticsDto;
import ru.otus.dto.filter.StaffEvaluationUserFilter;
import ru.otus.services.JwtService;
import ru.otus.services.UserService;
import ru.otus.services.ValueListService;
import ru.otus.services.staffevaluation.StaffEvaluationService;
import ru.otus.services.staffevaluationuser.StaffEvaluationServiceUser;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.entity.enums.StaffEvaluationStatus.ACTIVE;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.IN_PROGRESS;

@DisplayName("Контроллер статистики оценки персонала")
@WebMvcTest(StaffEvaluationStatisticsPageController.class)
@AutoConfigureMockMvc(addFilters = false)
class StaffEvaluationStatisticsPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ValueListService valueListService;

    @MockitoBean
    private StaffEvaluationService staffEvaluationService;

    @MockitoBean
    private StaffEvaluationServiceUser staffEvaluationServiceUser;

    @Test
    @DisplayName("страница должна отображать статистику и применять фильтры")
    void shouldRenderStatisticsWithFilters() throws Exception {
        var staffEvaluation = StaffEvaluationDto.builder()
            .id(5L)
            .name("Оценка 2026")
            .status(ACTIVE)
            .build();
        var statistics = new PageImpl<>(List.of(StaffEvaluationUserStatisticsDto.builder()
            .id(10L)
            .lastName("Иванов")
            .firstName("Иван")
            .username("ivanov")
            .status(IN_PROGRESS)
            .build()));
        when(staffEvaluationService.findById(5L)).thenReturn(staffEvaluation);
        when(staffEvaluationServiceUser.findStatistics(any(StaffEvaluationUserFilter.class), any()))
            .thenReturn(statistics);

        mvc.perform(get("/staff-evaluations/5/statistics")
                .param("search", "Иванов")
                .param("status", IN_PROGRESS.name()))
            .andExpect(status().isOk())
            .andExpect(view().name("page/staff-evaluation/statistics"))
            .andExpect(model().attribute("staffEvaluation", staffEvaluation))
            .andExpect(model().attribute("statistics", statistics))
            .andExpect(model().attributeExists("statuses", "filter"));

        var filterCaptor = ArgumentCaptor.forClass(StaffEvaluationUserFilter.class);
        verify(staffEvaluationServiceUser).findStatistics(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().staffEvaluationId()).isEqualTo(5L);
        assertThat(filterCaptor.getValue().search()).isEqualTo("Иванов");
        assertThat(filterCaptor.getValue().status()).isEqualTo(IN_PROGRESS);
    }
}
