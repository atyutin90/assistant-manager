package ru.otus.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.otus.entity.StaffEvaluationQuestion;

import javax.annotation.Nullable;
import java.util.List;

import static ru.otus.entity.StaffEvaluationQuestion.STAFF_EVALUATION_QUESTION_GRAPH;

@Repository
public interface StaffEvaluationQuestionRepository extends JpaRepository<StaffEvaluationQuestion, Long>,
    JpaSpecificationExecutor<StaffEvaluationQuestion> {

    @EntityGraph(value = STAFF_EVALUATION_QUESTION_GRAPH)
    @Override
    Page<StaffEvaluationQuestion> findAll(@Nullable Specification<StaffEvaluationQuestion> spec, Pageable pageable);

    @EntityGraph(value = STAFF_EVALUATION_QUESTION_GRAPH)
    List<StaffEvaluationQuestion> findByStaffEvaluationIdAndQuestionProjectRoleIdOrderByPositionAsc(
        Long staffEvaluationId,
        Long projectRoleId
    );
}
