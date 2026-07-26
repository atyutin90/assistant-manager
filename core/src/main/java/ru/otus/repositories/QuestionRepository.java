package ru.otus.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.otus.entity.Question;

import javax.annotation.Nullable;

import java.util.Optional;

import static ru.otus.entity.Question.QUESTION_GRAPH;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    @EntityGraph(value = QUESTION_GRAPH)
    @Override
    Page<Question> findAll(@Nullable Specification<Question> spec, Pageable pageable);

    @EntityGraph(value = QUESTION_GRAPH)
    @Override
    Optional<Question> findById(Long id);

    boolean existsByUuidAndIdNot(String uuid, Long id);
}
