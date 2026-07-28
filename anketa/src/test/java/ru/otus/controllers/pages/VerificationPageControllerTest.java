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
import ru.otus.dto.UserDto;
import ru.otus.dto.VerificationDetailsDto;
import ru.otus.dto.VerificationFormDto;
import ru.otus.dto.VerificationItemDto;
import ru.otus.dto.VerificationQuestionDto;
import ru.otus.services.JwtService;
import ru.otus.services.UserService;
import ru.otus.services.VerificationService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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
import static ru.otus.controllers.pages.VerificationPageControllerTest.LEAD;
import static ru.otus.entity.enums.AnswerResponse.YES;

@DisplayName("Контроллер проверки ответов")
@WebMvcTest(VerificationPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = LEAD)
class VerificationPageControllerTest {

    static final String LEAD = "lead";


    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private VerificationService verificationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUpCurrentUser() {
        when(userService.findByUsername(LEAD))
            .thenReturn(Optional.of(UserDto.builder().id(9L).username(LEAD).build()));
    }

    @Test
    @DisplayName("список должен содержать ожидающие проверки")
    void shouldRenderPendingVerifications() throws Exception {
        var page = new PageImpl<VerificationItemDto>(List.of());
        when(verificationService.findPending(any(), any())).thenReturn(page);

        mvc.perform(get("/verifications").with(user(LEAD)))
            .andExpect(status().isOk())
            .andExpect(view().name("page/verification/list"))
            .andExpect(model().attribute("verifications", page));
    }

    @Test
    @DisplayName("начало проверки должно перенаправлять на первый вопрос")
    void shouldRedirectToFirstQuestion() throws Exception {
        when(verificationService.findStartQuestion(12L, 9L)).thenReturn("question-1");

        mvc.perform(get("/verifications/12").with(user(LEAD)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/verifications/12/question-1"));
    }

    @Test
    @DisplayName("страница проверки должна содержать детали и форму")
    void shouldRenderVerificationDetails() throws Exception {
        var details = details();
        when(verificationService.findDetails(12L, 9L, "question-1")).thenReturn(details);

        mvc.perform(get("/verifications/12/question-1").with(user(LEAD)))
            .andExpect(status().isOk())
            .andExpect(view().name("page/verification/question"))
            .andExpect(model().attribute("verification", details))
            .andExpect(model().attributeExists("verificationForm"));
    }

    @Test
    @DisplayName("результат проверки должен сохраняться")
    void shouldSaveVerification() throws Exception {
        mvc.perform(post("/verifications/12/question-1")
                .with(user("lead"))
                .param("action", "next")
                .param("answerId", "21")
                .param("response", YES.name())
                .param("comment", "Согласен"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/verifications/12"));

        verify(verificationService).save(
            org.mockito.ArgumentMatchers.eq(12L),
            org.mockito.ArgumentMatchers.eq(9L),
            org.mockito.ArgumentMatchers.any(VerificationFormDto.class),
            org.mockito.ArgumentMatchers.eq(false)
        );
    }

    @Test
    @DisplayName("невалидный результат должен вернуть страницу проверки")
    void shouldRejectVerificationWithoutResponse() throws Exception {
        when(verificationService.findDetails(12L, 9L, "question-1")).thenReturn(details());

        mvc.perform(post("/verifications/12/question-1")
                .with(user(LEAD))
                .param("action", "finish")
                .param("answerId", "21"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/verification/question"))
            .andExpect(model().attributeHasErrors("verificationForm"));

        verify(verificationService, never()).save(any(), any(), any(), any(Boolean.class));
    }

    private VerificationDetailsDto details() {
        var question = VerificationQuestionDto.builder()
            .answerId(21L)
            .uuid("question-1")
            .areaKnowledge("Java")
            .section("Core")
            .question("Что такое JVM?")
            .employeeResponse(YES)
            .verifiedResponse(YES)
            .comment("Согласен")
            .build();
        return VerificationDetailsDto.builder()
            .staffAssignmentUserId(12L)
            .name("Оценка")
            .employeeName("Иванов Иван")
            .employeeUsername("employee")
            .questions(List.of(question))
            .currentQuestion(question)
            .verifiedQuestionsCount(1)
            .canFinish(true)
            .build();
    }
}
