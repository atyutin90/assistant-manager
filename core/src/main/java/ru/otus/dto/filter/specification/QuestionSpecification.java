package ru.otus.dto.filter.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.Question;
import ru.otus.entity.Skill;
import ru.otus.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Value
public class QuestionSpecification {

    public static Specification<Question> questionFilterSpecification(QuestionFilter filter) {
        return (root, query, cBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            filterByQuestionField(filter, root, cBuilder, predicates);

            filterByProjectRole(filter, root, cBuilder, predicates);

            filterBySkill(filter, root, cBuilder, predicates);

            return cBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void filterByQuestionField(QuestionFilter filter, Root<Question> root, CriteriaBuilder cBuilder,
                                              List<Predicate> predicates) {

        var search = filter.search();
        var enabled = filter.enabled();

        if (isNotEmpty(search)) {
            String trimmedSearch = search.trim();
            String likeSearch = "%" + trimmedSearch.toLowerCase() + "%";
            List<Predicate> searchPredicates = new ArrayList<>();

            searchPredicates.add(cBuilder.like(cBuilder.lower(root.get("uuid")), likeSearch));
            searchPredicates.add(cBuilder.like(cBuilder.lower(root.get("areaKnowledge")), likeSearch));
            searchPredicates.add(cBuilder.like(cBuilder.lower(root.get("section")), likeSearch));
            searchPredicates.add(cBuilder.like(cBuilder.lower(root.get("text")), likeSearch));

            parseLong(trimmedSearch).ifPresent(id -> searchPredicates.add(cBuilder.equal(root.get("id"), id)));

            predicates.add(cBuilder.or(searchPredicates.toArray(Predicate[]::new)));
        }

        if (enabled != null) {
            predicates.add(cBuilder.equal(root.get("enabled"), enabled));
        }
    }

    private static void filterByProjectRole(QuestionFilter filter, Root<Question> root, CriteriaBuilder cBuilder,
                                            List<Predicate> predicates) {
        if (isNotEmpty(filter.projectRole())) {
            Join<User, ProjectRole> projectRoleJoin = root.join("projectRole");
            predicates.add(cBuilder.equal(projectRoleJoin.get("id"), filter.projectRole()));
        }
    }

    private static void filterBySkill(QuestionFilter filter, Root<Question> root, CriteriaBuilder cBuilder,
                                      List<Predicate> predicates) {
        if (isNotEmpty(filter.skill())) {
            Join<User, Skill>skillJoin = root.join("skill");
            predicates.add(cBuilder.equal(skillJoin.get("id"), filter.skill()));
        }
    }

    private static Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
