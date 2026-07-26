package ru.otus.dto.filter.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.otus.dto.filter.StaffEvaluationQuestionFilter;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.Question;
import ru.otus.entity.Skill;
import ru.otus.entity.StaffEvaluation;
import ru.otus.entity.StaffEvaluationQuestion;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Value
public class StaffEvaluationQuestionSpecification {

    public static Specification<StaffEvaluationQuestion> staffEvaluationQuestionFilterSpecification(
        StaffEvaluationQuestionFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<StaffEvaluationQuestion, Question> questionJoin = root.join("question");

            filterByStaffEvaluation(filter, root, criteriaBuilder, predicates);

            filterByStaffEvaluationQuestionField(filter, criteriaBuilder, predicates, questionJoin);

            filterByProjectRole(filter, criteriaBuilder, questionJoin, predicates);

            filterBySkill(filter, criteriaBuilder, questionJoin, predicates);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void filterByStaffEvaluation(StaffEvaluationQuestionFilter filter,
                                                Root<StaffEvaluationQuestion> root, CriteriaBuilder criteriaBuilder,
                                                List<Predicate> predicates) {
        if (isNotEmpty(filter.staffEvaluationId())) {
            Join<StaffEvaluationQuestion, StaffEvaluation> evaluationJoin = root.join("staffEvaluation");
            predicates.add(criteriaBuilder.equal(evaluationJoin.get("id"), filter.staffEvaluationId()));
        }
    }

    private static void filterByStaffEvaluationQuestionField(StaffEvaluationQuestionFilter filter,
                                                             CriteriaBuilder criteriaBuilder,
                                                             List<Predicate> predicates,
                                                             Join<StaffEvaluationQuestion, Question> questionJoin) {
        if (StringUtils.hasText(filter.search())) {
            String search = "%" + filter.search().trim().toLowerCase() + "%";
            predicates.add(criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(questionJoin.get("uuid")), search),
                criteriaBuilder.like(criteriaBuilder.lower(questionJoin.get("areaKnowledge")), search),
                criteriaBuilder.like(criteriaBuilder.lower(questionJoin.get("section")), search),
                criteriaBuilder.like(criteriaBuilder.lower(questionJoin.get("text")), search)
            ));
        }
    }

    private static void filterByProjectRole(StaffEvaluationQuestionFilter filter, CriteriaBuilder criteriaBuilder,
                                            Join<StaffEvaluationQuestion, Question> questionJoin,
                                            List<Predicate> predicates) {
        if (isNotEmpty(filter.projectRole())) {
            Join<Question, ProjectRole> projectRoleJoin = questionJoin.join("projectRole");
            predicates.add(criteriaBuilder.equal(projectRoleJoin.get("id"), filter.projectRole()));
        }
    }

    private static void filterBySkill(StaffEvaluationQuestionFilter filter, CriteriaBuilder criteriaBuilder,
                                      Join<StaffEvaluationQuestion, Question> questionJoin,
                                      List<Predicate> predicates) {
        if (isNotEmpty(filter.skill())) {
            Join<Question, Skill> skillJoin = questionJoin.join("skill");
            predicates.add(criteriaBuilder.equal(skillJoin.get("id"), filter.skill()));
        }
    }
}
