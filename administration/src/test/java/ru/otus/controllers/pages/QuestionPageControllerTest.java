package ru.otus.controllers.pages;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import ru.otus.dto.QuestionDto;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.services.JwtService;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.SkillService;
import ru.otus.services.UserService;
import ru.otus.services.csv.CsvService;
import ru.otus.services.question.QuestionService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.FILTER;
import static ru.otus.controllers.pages.AbstractPageController.IS_EDIT;
import static ru.otus.controllers.pages.AbstractPageController.PROJECT_ROLES;
import static ru.otus.controllers.pages.AbstractPageController.QUESTION;
import static ru.otus.controllers.pages.AbstractPageController.QUESTIONS;
import static ru.otus.controllers.pages.AbstractPageController.SKILLS;

@DisplayName("Контроллер для вопросов ")
@WebMvcTest(QuestionPageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class QuestionPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProjectRoleService projectRoleService;

    @MockitoBean
    private SkillService skillService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private CsvService csvService;

    @DisplayName("страница вопросов должна отображаться с фильтром и справочниками")
    @Test
    void shouldRenderQuestionList() throws Exception {
        var page = new PageImpl<QuestionDto>(List.of());
        when(questionService.findAll(any(QuestionFilter.class), any())).thenReturn(page);

        mvc.perform(get("/questions")
                .param("search", "Spring")
                .param("projectRole", "2")
                .param("skill", "1")
                .param("enabled", "true"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/question/list"))
            .andExpect(model().attribute(QUESTIONS, page))
            .andExpect(model().attributeExists(FILTER, PROJECT_ROLES, SKILLS));
    }

    @DisplayName("форма новой вопроса должна отображаться с пустым DTO")
    @Test
    void shouldRenderNewCareerLevelPage() throws Exception {
        mvc.perform(get("/questions/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/question/form"))
            .andExpect(model().attribute(IS_EDIT, false))
            .andExpect(model().attributeExists(QUESTION, PROJECT_ROLES, SKILLS));
    }

    @DisplayName("новый вопрос должен сохраняться")
    @Test
    void shouldCreateQuestionAndRedirect() throws Exception {
        when(questionService.create(any(QuestionDto.class))).thenReturn(questionOf(10L));

        mvc.perform(post("/questions")
                .param("enabled", "true")
                .param("uuid", "question-10")
                .param("projectRole", "2")
                .param("skill", "1")
                .param("areaKnowledge", "Spring")
                .param("section", "MVC")
                .param("text", "Что такое MockMvc?"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/questions/10"));
        verify(questionService).create(any(QuestionDto.class));
    }

    @DisplayName("изменяемый вопрос должен сохраняться")
    @Test
    void shouldChangeQuestionAndRedirect() throws Exception {
        when(questionService.update(any(QuestionDto.class))).thenReturn(questionOf(1L));

        mvc.perform(post("/questions")
                .param("id", "1")
                .param("enabled", "true")
                .param("uuid", "question-10")
                .param("projectRole", "2")
                .param("skill", "1")
                .param("areaKnowledge", "Spring")
                .param("section", "MVC")
                .param("text", "Что такое MockMvc?"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/questions/1"));
        verify(questionService).update(any(QuestionDto.class));
    }

    @DisplayName("вопрос должен удаляться")
    @Test
    void shouldDeleteQuestion() throws Exception {
        mvc.perform(delete("/questions/10/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/questions"));
        verify(questionService).deleteById(10L);
    }

    @DisplayName("CSV с вопросами должен загружаться")
    @Test
    void shouldUploadQuestions() throws Exception {
        mvc.perform(multipart("/questions/upload")
                .file("file", "uuid;text".getBytes()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/questions"));
        verify(csvService).uploadQuestions(any(MultipartFile.class));
    }

    @DisplayName("CSV с вопросами должен скачиваться")
    @Test
    void shouldDownloadQuestions() throws Exception {
        when(csvService.downloadQuestions(any(QuestionFilter.class))).thenReturn(List.of());

        mvc.perform(get("/questions/download"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"questions.csv\""))
            .andExpect(content().contentType("text/csv; charset=UTF-8"));
    }

    private QuestionDto questionOf(Long id) {
        return QuestionDto.builder()
            .id(id)
            .enabled(true)
            .uuid("question-" + id)
            .projectRole(2L)
            .skill(1L)
            .areaKnowledge("Spring")
            .section("MVC")
            .text("Что такое MockMvc?")
            .build();
    }
}
