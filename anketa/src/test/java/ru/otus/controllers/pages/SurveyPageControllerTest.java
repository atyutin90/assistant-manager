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
import ru.otus.dto.FeedbackFormDto;
import ru.otus.dto.SurveyAnswerCommand;
import ru.otus.dto.SurveyPageDto;
import ru.otus.dto.SurveyQuestionDto;
import ru.otus.dto.UserDto;
import ru.otus.services.JwtService;
import ru.otus.services.SurveyService;
import ru.otus.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.SurveyPageControllerTest.EMPLOYEE;
import static ru.otus.entity.enums.AnswerResponse.YES;

@DisplayName("Контроллер прохождения опроса")
@WebMvcTest(SurveyPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = EMPLOYEE)
class SurveyPageControllerTest {

    static final String EMPLOYEE = "employee";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SurveyService surveyService;

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
    @DisplayName("старт опроса должен перенаправлять на первый вопрос")
    void shouldRedirectToFirstQuestion() throws Exception {
        when(surveyService.findStartQuestionUuid(3L, 7L, "DEVELOPER")).thenReturn("question-1");

        mvc.perform(get("/survey/3/DEVELOPER").with(user(EMPLOYEE)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/survey/3/DEVELOPER/question-1"));
    }

    @Test
    @DisplayName("страница вопроса должна содержать опрос и текущий ответ")
    void shouldRenderQuestion() throws Exception {
        var survey = survey();
        when(surveyService.findQuestion(3L, 7L, "DEVELOPER", "question-1")).thenReturn(survey);

        mvc.perform(get("/survey/3/DEVELOPER/question-1").with(user(EMPLOYEE)))
            .andExpect(status().isOk())
            .andExpect(view().name("page/survey/question"))
            .andExpect(model().attribute("survey", survey))
            .andExpect(model().attributeExists("answer"));
    }

    @Test
    @DisplayName("ответ должен сохраняться и продолжать опрос")
    void shouldSaveAnswerAndContinueSurvey() throws Exception {
        mvc.perform(post("/survey/3/DEVELOPER/question-1")
                .with(user(EMPLOYEE))
                .param("action", "next")
                .param("response", YES.name()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/survey/3/DEVELOPER"));

        verify(surveyService).saveAnswer(argThat(this::isExpectedAnswer));
    }

    @Test
    @DisplayName("завершение ответа должно возвращать к списку оценок")
    void shouldFinishSurvey() throws Exception {
        mvc.perform(post("/survey/3/DEVELOPER/complete")
                .with(user(EMPLOYEE)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/staff-evaluations"));

        verify(surveyService).complete(3L, 7L, "DEVELOPER");
    }

    @Test
    @DisplayName("пустой ответ должен вернуть страницу вопроса")
    void shouldRejectEmptyAnswer() throws Exception {
        when(surveyService.findQuestion(3L, 7L, "DEVELOPER", "question-1")).thenReturn(survey());

        mvc.perform(post("/survey/3/DEVELOPER/question-1")
                .with(user(EMPLOYEE))
                .param("action", "next"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/survey/question"))
            .andExpect(model().attributeHasErrors("answer"));

        verify(surveyService, never()).saveAnswer(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("страница обратной связи должна содержать сохранённую форму")
    void shouldRenderFeedback() throws Exception {
        var feedback = FeedbackFormDto.builder().message("Хороший опрос").build();
        when(surveyService.findFeedback(3L, 7L)).thenReturn(feedback);

        mvc.perform(get("/survey/3/feedback").with(user(EMPLOYEE)))
            .andExpect(status().isOk())
            .andExpect(view().name("page/survey/feedback"))
            .andExpect(model().attribute("feedback", feedback))
            .andExpect(model().attribute("staffEvaluationId", 3L));
    }

    @Test
    @DisplayName("обратная связь должна сохраняться")
    void shouldSaveFeedback() throws Exception {
        mvc.perform(post("/survey/3/feedback")
                .with(user(EMPLOYEE))
                .param("action", "finish")
                .param("message", "Хороший опрос"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/staff-evaluations"));

        verify(surveyService).saveFeedback(
            org.mockito.ArgumentMatchers.eq(3L),
            org.mockito.ArgumentMatchers.eq(7L),
            org.mockito.ArgumentMatchers.any(FeedbackFormDto.class),
            org.mockito.ArgumentMatchers.eq(true)
        );
    }

    private boolean isExpectedAnswer(SurveyAnswerCommand command) {
        var expected = SurveyAnswerCommand.builder()
            .staffEvaluationId(3L)
            .userId(7L)
            .projectRole("DEVELOPER")
            .questionUuid("question-1")
            .response(YES)
            .build();
        return expected.equals(command);
    }

    private SurveyPageDto survey() {
        var question = SurveyQuestionDto.builder().uuid("question-1").response(YES).build();
        return SurveyPageDto.builder()
            .staffEvaluationId(3L)
            .evaluationName("Оценка")
            .projectRole("DEVELOPER")
            .questions(List.of(question))
            .currentQuestion(question)
            .currentNumber(1)
            .build();
    }
}
