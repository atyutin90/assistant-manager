package ru.otus.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.dto.LatestAnswerProjection;
import ru.otus.entity.StaffEvaluationAnswer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Query(value = """
        WITH ranked_answers AS (
            SELECT
                seu.user_id AS user_id,
                sea.question_id AS question_id,
                q.text AS question_text,
                q.project_role_id AS project_role_id,        
                ROW_NUMBER() OVER (
                    PARTITION BY seu.user_id, sea.question_id
                    ORDER BY sea.id DESC
                ) AS answer_rank
            FROM staff_evaluation_answer sea
            LEFT JOIN staff_evaluation_user seu ON seu.id = sea.staff_evaluation_user_id
            LEFT JOIN staff_evaluation se ON se.id = seu.staff_evaluation_id
            LEFT JOIN question q ON q.id = sea.question_id
            WHERE seu.status = 'COMPLETED'
              AND se.status = 'COMPLETED'
              AND sea.response = 'YES'
              AND sea.verified_response = 'YES'
              AND sea.question_id in (:questionIds)
        )
        SELECT user_id, question_id, project_role_id, question_text
        FROM ranked_answers
        WHERE answer_rank = 1
        """, nativeQuery = true)
    List<LatestAnswerProjection> findLatestPositiveAnswers(@Param("questionIds") Set<Long> questionIds);
}
