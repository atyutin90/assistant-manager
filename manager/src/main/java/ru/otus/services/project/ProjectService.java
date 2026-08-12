package ru.otus.services.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.ManagerAccessDto;
import ru.otus.dto.ProjectDto;
import ru.otus.dto.filter.ProjectFilter;
import ru.otus.dto.ProjectQuestionLevelDto;
import ru.otus.dto.ProjectQuestionsForm;
import ru.otus.dto.filter.QuestionFilter;

import java.util.List;
import java.util.Set;

public interface ProjectService {

    Page<ProjectDto> findAll(ProjectFilter filter, Pageable pageable);

    List<ProjectDto> findAll(ProjectFilter filter);

    ProjectDto findById(Long id, Long userId);

    ProjectDto save(ProjectDto project, Long userId);

    void copy(Long id, Long userId);

    void deleteById(Long id, Long userId);

    Page<ProjectQuestionLevelDto> findQuestions(Long projectId, Long userId, QuestionFilter filter, Pageable pageable);

    void saveQuestions(Long projectId, Long userId, Long projectRoleId, ProjectQuestionsForm form);

    ProjectDto findEditableById(Long id, Long userId);

    List<ManagerAccessDto> findManagerAccessOptions(Long projectId, Long userId);

    void saveAccess(Long projectId, Long userId, Set<Long> readManagerIds, Set<Long> editManagerIds);

    boolean canEdit(Long projectId, Long userId);
}
