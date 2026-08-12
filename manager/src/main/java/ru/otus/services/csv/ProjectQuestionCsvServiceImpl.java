package ru.otus.services.csv;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.otus.dto.CsvProjectQuestionDto;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.entity.AssessmentProjectQuestion;
import ru.otus.entity.CareerLevel;
import ru.otus.entity.Question;
import ru.otus.exceptions.UploadFileException;
import ru.otus.exceptions.WebApplicationException;
import ru.otus.repositories.AssessmentProjectQuestionRepository;
import ru.otus.repositories.AssessmentProjectRepository;
import ru.otus.repositories.CareerLevelRepository;
import ru.otus.repositories.QuestionRepository;
import ru.otus.services.project.ProjectService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static ru.otus.controllers.pages.AbstractPageController.UUID;
import static ru.otus.controllers.pages.DownloadPageController.SEMICOLON;
import static ru.otus.dto.filter.specification.QuestionSpecification.questionFilterSpecification;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static ru.otus.exceptions.WebApplicationException.errorOf;

@Service
@RequiredArgsConstructor
public class ProjectQuestionCsvServiceImpl implements ProjectQuestionCsvService {

    private final ProjectService projectService;

    private final QuestionRepository questionRepository;

    private final CareerLevelRepository careerLevelRepository;

    private final AssessmentProjectQuestionRepository projectQuestionRepository;

    private final AssessmentProjectRepository projectRepository;

    private final MessageSource messageSource;

    @Override
    public List<CsvProjectQuestionDto> download(Long projectId, Long userId, QuestionFilter filter) {
        projectService.findById(projectId, userId);
        var questions = questionRepository.findAll(questionFilterSpecification(filter), Sort.by(UUID));
        var assessmentProjectQuestionMap = isNotEmpty(questions) ?
            assessmentProjectQuestionMapOf(assessmentProjectQuestionsOf(projectId, questions)) :
            Map.<Long, AssessmentProjectQuestion>of();
        return questions.stream()
            .map(question -> csvQuestionOf(question, assessmentProjectQuestionMap.get(question.getId())))
            .toList();
    }

    @Override
    @Transactional
    public void upload(Long projectId, Long userId, MultipartFile file) {
        projectService.findEditableById(projectId, userId);
        if (file == null || file.isEmpty()) {
            return;
        }
        try (var reader = bufferedReaderOf(file)) {
            var rows = new CsvToBeanBuilder<CsvProjectQuestionDto>(reader)
                .withType(CsvProjectQuestionDto.class)
                .withSeparator(SEMICOLON)
                .withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                .build()
                .parse();
            synchronize(projectId, rows);
        } catch (WebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UploadFileException(
                messageSource.getMessage("error.upload-data", new Object[]{}, getLocale()), ex
            );
        }
    }

    private List<AssessmentProjectQuestion> assessmentProjectQuestionsOf(Long projectId, List<Question> questions) {
        var questionIds = questions.stream().map(Question::getId).toList();
        return projectQuestionRepository.findByProjectIdAndQuestionIdIn(projectId, questionIds);
    }

    private void synchronize(Long projectId, List<CsvProjectQuestionDto> rows) {
        var assessmentProjectQuestions = projectQuestionRepository.findByProjectId(projectId);
        var assessmentProjectQuestionMap = assessmentProjectQuestionMapOf(assessmentProjectQuestions);
        var importedUuids = importedUuidsOf(rows);

        if (importedUuids.stream().distinct().count() != importedUuids.size()) {
            throw errorOf(
                NOT_FOUND,
                messageSource.getMessage("error.question-dublicate-uuid", new Object[]{},
                    getLocale())
            );
        }

        rows.forEach(row ->
            synchronizeRow(projectId, row, questionMapOf(), careerLevelMapOf(), assessmentProjectQuestionMap)
        );
    }

    private List<String> importedUuidsOf(List<CsvProjectQuestionDto> rows) {
        return rows.stream()
            .map(CsvProjectQuestionDto::getUuid)
            .map(this::valueOf)
            .toList();
    }

    private Map<Long, AssessmentProjectQuestion> assessmentProjectQuestionMapOf(
        List<AssessmentProjectQuestion> assessmentProjectQuestions
    ) {
        return assessmentProjectQuestions
            .stream()
            .collect(toMap(apq -> apq.getQuestion().getId(), identity()));
    }

    private Map<String, CareerLevel> careerLevelMapOf() {
        return careerLevelRepository.findAll().stream()
            .collect(toMap(CareerLevel::getCode, identity()));
    }

    private Map<String, Question> questionMapOf() {
        return questionRepository.findAll().stream()
            .collect(toMap(Question::getUuid, identity()));
    }

    private void synchronizeRow(Long projectId, CsvProjectQuestionDto row,
                                Map<String, Question> questionMap,
                                Map<String, CareerLevel> careerLevelMap,
                                Map<Long, AssessmentProjectQuestion> assessmentProjectQuestionMap) {

        var question = questionOf(row, questionMap);
        var careerLevel = careerLevelOf(row, careerLevelMap);
        var assessmentProjectQuestion = assessmentProjectQuestionMap.get(question.getId());
        if (isNull(careerLevel)) {
            if (nonNull(assessmentProjectQuestion)) {
                projectQuestionRepository.delete(assessmentProjectQuestion);
            }
            return;
        }
        if (isNull(assessmentProjectQuestion)) {
            assessmentProjectQuestion = AssessmentProjectQuestion.builder()
                .project(projectRepository.getReferenceById(projectId))
                .question(question)
                .careerLevel(careerLevel)
                .build();
        } else {
            assessmentProjectQuestion.setCareerLevel(careerLevel);
        }
        projectQuestionRepository.save(assessmentProjectQuestion);
    }

    private CareerLevel careerLevelOf(CsvProjectQuestionDto row, Map<String, CareerLevel> careerLevelMap) {
        CareerLevel result = null;
        if (StringUtils.isNotEmpty(row.getCareerLevel())) {
            result = careerLevelMap.get(valueOf(row.getCareerLevel()));
            if (isNull(result)) {
                throw errorOf(
                    NOT_FOUND,
                    messageSource.getMessage("error.unknown-career-level", new Object[]{row.getCareerLevel()},
                        getLocale())
                );
            }
        }
        return result;
    }

    private Question questionOf(CsvProjectQuestionDto row, Map<String, Question> questionMap) {
        var question = questionMap.get(valueOf(row.getUuid()));
        if (isNull(question)) {
            throw errorOf(
                NOT_FOUND,
                messageSource.getMessage("error.question-unknown-uuid", new Object[]{row.getUuid()},
                    getLocale())
            );
        }
        return question;
    }

    private String valueOf(String value) {
        return StringUtils.isNotEmpty(value) ? value.trim() : EMPTY;
    }

    private static BufferedReader bufferedReaderOf(MultipartFile file) throws IOException {
        return new BufferedReader(
            new InputStreamReader(BOMInputStream.builder().setInputStream(file.getInputStream()).get(), UTF_8)
        );
    }

    private CsvProjectQuestionDto csvQuestionOf(Question question, AssessmentProjectQuestion assignment) {
        return CsvProjectQuestionDto.builder()
            .uuid(question.getUuid())
            .projectRole(question.getProjectRole().getCode())
            .skill(question.getSkill() != null ? question.getSkill().getCode() : null)
            .areaKnowledge(question.getAreaKnowledge())
            .section(question.getSection())
            .text(question.getText())
            .careerLevel(assignment != null ? assignment.getCareerLevel().getCode() : null)
            .build();
    }
}
