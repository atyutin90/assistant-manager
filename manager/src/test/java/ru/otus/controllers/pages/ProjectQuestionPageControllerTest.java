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
import org.springframework.web.multipart.MultipartFile;
import ru.otus.dto.CsvProjectQuestionDto;
import ru.otus.dto.ProjectDto;
import ru.otus.dto.ProjectQuestionLevelDto;
import ru.otus.dto.ProjectQuestionsForm;
import ru.otus.dto.UserDto;
import ru.otus.services.CareerLevelService;
import ru.otus.services.JwtService;
import ru.otus.services.ProjectRoleService;
import ru.otus.services.csv.ProjectQuestionCsvService;
import ru.otus.services.project.ProjectService;
import ru.otus.services.SkillService;
import ru.otus.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.otus.controllers.pages.AbstractPageController.CAREER_LEVELS;
import static ru.otus.controllers.pages.AbstractPageController.FILTER;
import static ru.otus.controllers.pages.AbstractPageController.PROJECT;
import static ru.otus.controllers.pages.AbstractPageController.PROJECT_ROLES;
import static ru.otus.controllers.pages.AbstractPageController.SKILLS;
import static ru.otus.controllers.pages.AbstractPageController.SUCCESS_OPERATION;

@DisplayName("Контроллер вопросов проекта")
@WebMvcTest(ProjectQuestionPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "manager")
class ProjectQuestionPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectRoleService projectRoleService;

    @MockitoBean
    private CareerLevelService careerLevelService;

    @MockitoBean
    private SkillService skillService;

    @MockitoBean
    private ProjectQuestionCsvService csvService;

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
    @DisplayName("страница должна содержать вопросы проекта и справочники")
    void shouldRenderProjectQuestions() throws Exception {
        var question = ProjectQuestionLevelDto.builder().questionId(11L).uuid("question-1").build();
        var page = new PageImpl<>(List.of(question));
        when(projectService.findQuestions(eq(3L), eq(5L), any(), any())).thenReturn(page);
        when(projectService.findById(3L, 5L))
            .thenReturn(ProjectDto.builder().id(3L).name("CRM").active(true).build());

        mvc.perform(get("/projects/3/questions")
                .with(user("manager"))
                .param("projectRole", "2")
                .param("search", "Spring"))
            .andExpect(status().isOk())
            .andExpect(view().name("page/project/questions"))
            .andExpect(model().attributeExists(
                PROJECT, "questionLevels", "questionPage", FILTER, PROJECT_ROLES, CAREER_LEVELS, SKILLS
            ));

        verify(projectService).findQuestions(eq(3L), eq(5L), any(), any());
    }

    @Test
    @DisplayName("уровни вопросов проекта должны сохраняться")
    void shouldSaveProjectQuestions() throws Exception {
        mvc.perform(post("/projects/3/questions")
                .with(user("manager"))
                .param("projectRole", "2")
                .param("search", "Spring")
                .param("skill", "4")
                .param("enabled", "true")
                .param("page", "2")
                .param("questions[0].questionId", "11")
                .param("questions[0].careerLevelId", "6"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(
                "/projects/3/questions?page=2&search=Spring&projectRole=2&skill=4&enabled=true"
            ))
            .andExpect(flash().attribute(SUCCESS_OPERATION, true));

        verify(projectService).saveQuestions(
            eq(3L), eq(5L), eq(2L), any(ProjectQuestionsForm.class)
        );
    }

    @Test
    @DisplayName("CSV с уровнями вопросов проекта должен загружаться")
    void shouldUploadProjectQuestions() throws Exception {
        mvc.perform(multipart("/projects/3/questions/upload")
                .file("file", "uuid;career-level".getBytes())
                .with(user("manager")))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/projects/3/questions"))
            .andExpect(flash().attribute(SUCCESS_OPERATION, true));

        verify(csvService).upload(eq(3L), eq(5L), any(MultipartFile.class));
    }

    @Test
    @DisplayName("CSV с уровнями вопросов проекта должен скачиваться с учетом фильтров")
    void shouldDownloadProjectQuestions() throws Exception {
        when(csvService.download(eq(3L), eq(5L), any())).thenReturn(List.of(
            CsvProjectQuestionDto.builder()
                .uuid("question-1")
                .projectRole("Developer")
                .skill("4")
                .areaKnowledge("Java")
                .section("Core")
                .text("Question")
                .careerLevel("MIDDLE")
                .build()
        ));

        mvc.perform(get("/projects/3/questions/download")
                .with(user("manager")))
            .andExpect(status().isOk())
            .andExpect(header().string(
                "Content-Disposition", "attachment; filename=\"project-questions.csv\""
            ))
            .andExpect(content().contentType("text/csv; charset=UTF-8"));

        verify(csvService).download(eq(3L), eq(5L), any());
    }
}
