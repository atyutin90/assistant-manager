package ru.otus.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.otus.entity.StaffEvaluation;

@Repository
public interface StaffEvaluationRepository extends
    JpaRepository<StaffEvaluation, Long>,
    JpaSpecificationExecutor<StaffEvaluation> {
}
