package ru.otus.services.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import ru.otus.dto.ProjectDto;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.entity.AssessmentProject;
import ru.otus.entity.AssessmentProjectQuestion;
import ru.otus.entity.CareerLevel;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.Question;
import ru.otus.entity.Skill;
import ru.otus.repositories.AssessmentProjectQuestionRepository;
import ru.otus.repositories.AssessmentProjectRepository;
import ru.otus.repositories.CareerLevelRepository;
import ru.otus.repositories.QuestionRepository;
import ru.otus.services.project.ProjectService;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CSV-сервис вопросов проекта")
class ProjectQuestionCsvServiceImplTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private CareerLevelRepository careerLevelRepository;

    @Mock
    private AssessmentProjectQuestionRepository projectQuestionRepository;

    @Mock
    private AssessmentProjectRepository projectRepository;

    @Mock
    private MessageSource messageSource;

    private ProjectQuestionCsvServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProjectQuestionCsvServiceImpl(projectService, questionRepository, careerLevelRepository,
            projectQuestionRepository, projectRepository, messageSource);
    }

    @Test
    @DisplayName("выгрузка должна содержать наименования и назначенный карьерный уровень")
    @SuppressWarnings("unchecked")
    void shouldDownloadQuestionLevels() {
        var question = question();
        var level = CareerLevel.builder()
            .id(1L)
            .name("Intern")
            .code("INTERN")
            .build();
        var assignment = AssessmentProjectQuestion.builder()
            .question(question)
            .careerLevel(level)
            .build();
        when(questionRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(List.of(question));
        when(projectQuestionRepository.findByProjectIdAndQuestionIdIn(1L, List.of(1L)))
            .thenReturn(List.of(assignment));

        var result = service.download(1L, 1L, QuestionFilter.builder().build());

        assertEquals(1, result.size());
        assertEquals("DEV", result.get(0).getProjectRole());
        assertEquals("JAVA", result.get(0).getSkill());
        assertEquals("INTERN", result.get(0).getCareerLevel());
        verify(projectService).findById(1L, 1L);
    }

    @Test
    @DisplayName("загрузка должна назначать карьерный уровень по uuid вопроса и наименованию уровня")
    void shouldUploadQuestionLevel() {
        var project = AssessmentProject.builder()
            .id(1L)
            .build();
        var question = question();
        var level = CareerLevel.builder()
            .id(1L)
            .code("INTERN")
            .name("Intern")
            .build();
        var csv = csv();
        when(projectService.findEditableById(1L, 1L))
            .thenReturn(ProjectDto.builder().id(1L).build());
        when(questionRepository.findAll())
            .thenReturn(List.of(question));
        when(careerLevelRepository.findAll())
            .thenReturn(List.of(level));
        when(projectQuestionRepository.findByProjectId(1L))
            .thenReturn(List.of());
        when(projectRepository.getReferenceById(1L))
            .thenReturn(project);

        service.upload(1L, 1L, new MockMultipartFile("file", "project-questions.csv", "text/csv", csv.getBytes(UTF_8)));

        var captor = ArgumentCaptor.forClass(AssessmentProjectQuestion.class);
        verify(projectQuestionRepository).save(captor.capture());
        assertEquals(question, captor.getValue().getQuestion());
        assertEquals(level, captor.getValue().getCareerLevel());
        assertEquals(project, captor.getValue().getProject());
        verify(projectService).findEditableById(1L, 1L);
    }

    private static Question question() {
        return Question.builder()
            .id(1L)
            .uuid("UUID")
            .projectRole(ProjectRole.builder().
                code("DEV")
                .name("Developer")
                .build())
            .skill(Skill.builder()
                .code("JAVA")
                .name("Java")
                .build())
            .areaKnowledge("Core")
            .section("Basics")
            .text("Question")
            .build();
    }

    private static String csv() {
        return "uuid;project-role;skill;area-knowledge;section;text;career-level\n"
            + "UUID;DEV;JAVA;Core;Basics;Question;INTERN\n";
    }
}
