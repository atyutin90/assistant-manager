package ru.otus.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.otus.entity.StaffEvaluationAnswer;

import java.util.List;
import java.util.Optional;

import static ru.otus.entity.StaffEvaluationAnswer.STAFF_EVALUATION_ANSWER_GRAPH;

@Repository
public interface StaffEvaluationAnswerRepository extends JpaRepository<StaffEvaluationAnswer, Long> {

    @EntityGraph(value = STAFF_EVALUATION_ANSWER_GRAPH)
    List<StaffEvaluationAnswer> findByStaffEvaluationUserId(Long staffEvaluationUserId);

    @EntityGraph(value = STAFF_EVALUATION_ANSWER_GRAPH)
    Optional<StaffEvaluationAnswer> findByStaffEvaluationUserIdAndQuestionId(
        Long staffEvaluationUserId,
        Long questionId
    );
}
