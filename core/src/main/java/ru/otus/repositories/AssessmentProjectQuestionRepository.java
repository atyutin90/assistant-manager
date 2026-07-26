package ru.otus.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.otus.entity.AssessmentProjectQuestion;

import java.util.List;

import static ru.otus.entity.AssessmentProjectQuestion.ASSESSMENT_PROJECT_QUESTION_GRAPH;

@Repository
public interface AssessmentProjectQuestionRepository
    extends JpaRepository<AssessmentProjectQuestion, Long> {

    @EntityGraph(value = ASSESSMENT_PROJECT_QUESTION_GRAPH)
    List<AssessmentProjectQuestion> findByProjectIdAndQuestionIdIn(Long projectId, List<Long> questionIds);

    @EntityGraph(value = ASSESSMENT_PROJECT_QUESTION_GRAPH)
    List<AssessmentProjectQuestion> findByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
