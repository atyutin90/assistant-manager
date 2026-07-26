package ru.otus.dto.filter.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.otus.dto.filter.StaffEvaluationFilter;
import ru.otus.entity.StaffEvaluation;

import java.util.ArrayList;
import java.util.List;

@Value
public class StaffEvaluationSpecification {

    public static Specification<StaffEvaluation> staffEvaluationFilterSpecification(StaffEvaluationFilter filter) {

        var search = filter.search();

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String trimmedSearch = search.trim();
                String likeSearch = "%" + trimmedSearch.toLowerCase() + "%";
                List<Predicate> searchPredicates = new ArrayList<>();
                searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likeSearch));
                predicates.add(criteriaBuilder.or(searchPredicates.toArray(Predicate[]::new)));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
