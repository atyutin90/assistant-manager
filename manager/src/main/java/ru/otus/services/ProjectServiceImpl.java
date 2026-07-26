package ru.otus.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.dto.ManagerAccessDto;
import ru.otus.dto.ProjectDto;
import ru.otus.dto.filter.ProjectFilter;
import ru.otus.dto.ProjectQuestionLevelDto;
import ru.otus.dto.ProjectQuestionsForm;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.entity.AssessmentProject;
import ru.otus.entity.AssessmentProjectQuestion;
import ru.otus.entity.CareerLevel;
import ru.otus.entity.Question;
import ru.otus.entity.User;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.repositories.AssessmentProjectQuestionRepository;
import ru.otus.repositories.AssessmentProjectRepository;
import ru.otus.repositories.CareerLevelRepository;
import ru.otus.repositories.QuestionRepository;
import ru.otus.repositories.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.lang.Math.min;
import static java.util.Set.of;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.dto.filter.specification.ProjectSpecification.projectFilterSpecification;
import static ru.otus.dto.filter.specification.QuestionSpecification.questionFilterSpecification;
import static ru.otus.entity.enums.UserRole.MANAGER;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private static final String COPY_SUFFIX = "-copy";

    private static final String NAME = "name";

    private static final int PROJECT_NAME_MAX_LENGTH = 255;

    private final AssessmentProjectRepository projectRepository;

    private final AssessmentProjectQuestionRepository projectQuestionRepository;

    private final QuestionRepository questionRepository;

    private final CareerLevelRepository careerLevelRepository;

    private final UserRepository userRepository;

    private final MessageSource messageSource;

    @Override
    public Page<ProjectDto> findAll(ProjectFilter filter, Pageable pageable) {
        return projectRepository.findAll(projectFilterSpecification(filter), pageable)
            .map(this::dtoOf);
    }

    @Override
    public List<ProjectDto> findAll(ProjectFilter filter) {
        return projectRepository.findAll(projectFilterSpecification(filter), Sort.by(NAME)).stream()
            .map(this::dtoOf)
            .toList();
    }

    @Override
    public ProjectDto findById(Long id, Long userId) {
        return dtoOf(findAccessibleProject(id, userId));
    }

    @Override
    @Transactional
    public ProjectDto save(ProjectDto data, Long userId) {
        AssessmentProject project;
        if (data.id() == null) {
            var owner = userOf(userId);
            validateUniqueName(data, owner.getId());
            project = AssessmentProject.builder()
                .name(data.name())
                .description(data.description())
                .active(data.active())
                .owner(owner)
                .build();
        } else {
            project = findAccessibleProject(data.id(), userId);
            validateUniqueName(data, project.getOwner().getId());
            project.setName(data.name());
            project.setDescription(data.description());
            project.setActive(data.active());
        }
        return dtoOf(projectRepository.save(project));
    }

    @Override
    @Transactional
    public void copy(Long id, Long userId) {
        var project = findAccessibleProject(id, userId);
        var owner = userOf(userId);
        var copyProject = AssessmentProject.builder()
            .name(copyName(project.getName(), owner.getId()))
            .description(project.getDescription())
            .active(project.getActive())
            .owner(owner)
            .build();
        var savedCopyProject = projectRepository.save(copyProject);
        var copiedSettings = projectQuestionRepository.findByProjectId(project.getId()).stream()
            .map(setting -> AssessmentProjectQuestion.builder()
                .project(savedCopyProject)
                .question(setting.getQuestion())
                .careerLevel(setting.getCareerLevel())
                .build())
            .toList();
        projectQuestionRepository.saveAll(copiedSettings);
    }

    @Override
    @Transactional
    public void deleteById(Long id, Long userId) {
        var project = getByIdAndOwnerId(id, userId);
        projectQuestionRepository.deleteByProjectId(id);
        projectRepository.delete(project);
    }

    @Override
    public Page<ProjectQuestionLevelDto> findQuestions(Long projectId, Long userId,
                                                       QuestionFilter filter, Pageable pageable) {
        findAccessibleProject(projectId, userId);
        var questions = questionRepository.findAll(questionFilterSpecification(filter), pageable);
        var questionIds = questions.stream().map(Question::getId).toList();
        var assignedLevelMap = assignedLevelMapOf(projectId, questionIds);
        return questions.map(question -> questionOf(question, assignedLevelMap.get(question.getId())));
    }

    @Override
    @Transactional
    public void saveQuestions(Long projectId, Long userId, Long projectRoleId, ProjectQuestionsForm form) {
        var project = findAccessibleProject(projectId, userId);
        var questionMap = questionsMap(form, projectRoleId);
        var existProjectQuestionMap = assignedLevelMapOf(projectId, questionMap.keySet().stream().toList());
        var deleteProjectQuestions = getDeleteProjectQuestions(form, existProjectQuestionMap);
        var upsertProjectQuestions = getUpsertProjectQuestions(form, project, existProjectQuestionMap, questionMap);
        projectQuestionRepository.deleteAll(deleteProjectQuestions);
        projectQuestionRepository.saveAll(upsertProjectQuestions);
    }

    @Override
    public ProjectDto findByIdAndOwnerId(Long id, Long userId) {
        return dtoOf(getByIdAndOwnerId(id, userId));
    }

    @Override
    public List<ManagerAccessDto> findManagerAccessOptions(Long projectId, Long userId) {
        var project = getByIdAndOwnerId(projectId, userId);
        var selectedIds = project.getSharedManagers().stream()
            .map(User::getId)
            .collect(toSet());
        return userRepository.findByRolesContainsAndIdNot(MANAGER, project.getOwner().getId()).stream()
            .map(manager -> managerOf(manager, selectedIds))
            .toList();
    }

    @Override
    @Transactional
    public void saveAccess(Long projectId, Long userId, Set<Long> managerIds) {
        var project = getByIdAndOwnerId(projectId, userId);
        var requestedIds = managerIds == null ? of() : managerIds;
        var managers = userRepository.findByRolesContainsAndIdNot(MANAGER, project.getOwner().getId()).stream()
            .filter(manager -> requestedIds.contains(manager.getId()))
            .collect(toSet());
        project.getSharedManagers().clear();
        project.getSharedManagers().addAll(managers);
        projectRepository.save(project);
    }

    private List<AssessmentProjectQuestion> getUpsertProjectQuestions(
        ProjectQuestionsForm form,
        AssessmentProject project,
        Map<Long, AssessmentProjectQuestion> existProjectQuestionMap,
        Map<Long, Question> questionMap
    ) {
        var careerLevelMap = careerLevelMap(form);
        return form.questions().stream()
            .distinct()
            .map(value -> {
                AssessmentProjectQuestion result = null;
                var projectQuestion = existProjectQuestionMap.get(value.questionId());
                if (projectQuestion != null && value.careerLevelId() != null) {
                    projectQuestion.setCareerLevel(careerLevelMap.get(value.careerLevelId()));
                    result = projectQuestion;
                } else if (projectQuestion == null) {
                    result = AssessmentProjectQuestion.builder()
                        .project(project)
                        .question(questionMap.get(value.questionId()))
                        .careerLevel(careerLevelMap.get(value.careerLevelId()))
                        .build();
                }
                return result;
            }).filter(Objects::nonNull)
            .toList();
    }

    private static List<AssessmentProjectQuestion> getDeleteProjectQuestions(
        ProjectQuestionsForm form,
        Map<Long, AssessmentProjectQuestion> existProjectQuestionMap
    ) {
        return form.questions().stream()
            .distinct()
            .map(value -> {
                AssessmentProjectQuestion result = null;
                var projectQuestion = existProjectQuestionMap.get(value.questionId());
                if (projectQuestion != null && value.careerLevelId() == null) {
                    result = projectQuestion;
                }
                return result;
            }).filter(Objects::nonNull)
            .toList();
    }

    private Map<Long, Question> questionsMap(ProjectQuestionsForm form, Long projectRoleId) {
        var requestedIds = form.questions().stream()
            .map(ProjectQuestionLevelDto::questionId)
            .collect(toSet());
        return questionRepository.findAllById(requestedIds).stream()
            .filter(question -> question.getProjectRole().getId().equals(projectRoleId))
            .collect(toMap(Question::getId, identity()));
    }

    private Map<Long, CareerLevel> careerLevelMap(ProjectQuestionsForm form) {
        return careerLevelRepository.findAllById(
                form.questions().stream()
                    .map(ProjectQuestionLevelDto::careerLevelId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList()
            ).stream()
            .filter(level -> TRUE.equals(level.getEnabled()))
            .collect(toMap(CareerLevel::getId, identity()));
    }

    private AssessmentProject findAccessibleProject(Long id, Long managerId) {
        return projectRepository.findAccessibleById(id, managerId).orElseThrow(() -> notFound(id));
    }

    private AssessmentProject getByIdAndOwnerId(Long id, Long managerId) {
        return projectRepository.findByIdAndOwnerId(id, managerId).orElseThrow(() -> notFound(id));
    }

    private User userOf(Long managerId) {
        return userRepository.findById(managerId).orElseThrow(() -> notFound(managerId));
    }

    private DataNotFoundException notFound(Object value) {
        return new DataNotFoundException(
            messageSource.getMessage("error.not-found-project-with-id", new Object[]{value}, getLocale())
        );
    }

    private void validateUniqueName(ProjectDto data, Long ownerId) {
        boolean exists = data.id() == null
            ? projectRepository.existsByOwnerIdAndNameIgnoreCase(ownerId, data.name())
            : projectRepository.existsByOwnerIdAndNameIgnoreCaseAndIdNot(
            ownerId,
            data.name(),
            data.id()
        );
        if (exists) {
            throw new NonUniqueValueException(Map.of(
                "name",
                messageSource.getMessage("error.non-unique-value", null, getLocale())
            ));
        }
    }

    private String copyName(String sourceName, Long ownerId) {
        int copyNumber = 1;
        String candidate;
        do {
            var number = copyNumber == 1 ? EMPTY : "-" + copyNumber;
            var suffix = number + COPY_SUFFIX;
            int sourceLength = PROJECT_NAME_MAX_LENGTH - suffix.length();
            candidate = sourceName.substring(0, min(sourceName.length(), sourceLength)) + suffix;
            copyNumber++;
        } while (projectRepository.existsByOwnerIdAndNameIgnoreCase(ownerId, candidate));
        return candidate;
    }

    private ProjectDto dtoOf(AssessmentProject project) {
        var owner = project.getOwner();
        return ProjectDto.builder()
            .id(project.getId())
            .name(project.getName())
            .description(project.getDescription())
            .active(project.getActive())
            .ownerId(owner != null ? owner.getId() : null)
            .ownerUsername(owner != null ? owner.getUsername() : null)
            .build();
    }

    private ManagerAccessDto managerOf(User manager, Set<Long> selectedIds) {
        return ManagerAccessDto.builder()
            .id(manager.getId())
            .name(manager.getDisplayName())
            .username(manager.getUsername())
            .selected(selectedIds.contains(manager.getId()))
            .build();
    }

    private Map<Long, AssessmentProjectQuestion> assignedLevelMapOf(Long projectId, List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return projectQuestionRepository
            .findByProjectIdAndQuestionIdIn(projectId, questionIds)
            .stream()
            .collect(toMap(value -> value.getQuestion().getId(), identity()));
    }

    private ProjectQuestionLevelDto questionOf(Question question, AssessmentProjectQuestion assignedLevel) {
        return ProjectQuestionLevelDto.builder()
            .questionId(question.getId())
            .enabled(question.getEnabled())
            .uuid(question.getUuid())
            .projectRole(question.getProjectRole().getName())
            .skill(question.getSkill().getName())
            .areaKnowledge(question.getAreaKnowledge())
            .section(question.getSection())
            .text(question.getText())
            .careerLevelId(assignedLevel != null ? assignedLevel.getCareerLevel().getId() : null)
            .build();
    }
}
