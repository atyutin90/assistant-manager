package ru.otus.dto.filter.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.otus.dto.filter.TechnologyFilter;
import ru.otus.entity.Technology;

import java.util.ArrayList;
import java.util.List;

@Value
public class TechnologySpecification {

    public static Specification<Technology> technologyFilterSpecification(TechnologyFilter filter) {

        var search = filter.search();

        var enabled = filter.enabled();

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            filterByTechnologyNameField(root, criteriaBuilder, search, predicates);
            filterByTechnologyEnabledField(root, criteriaBuilder, enabled, predicates);
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void filterByTechnologyNameField(Root<Technology> root, CriteriaBuilder criteriaBuilder,
                                                    String search, List<Predicate> predicates) {
        if (StringUtils.hasText(search)) {
            String trimmedSearch = search.trim();
            String likeSearch = "%" + trimmedSearch.toLowerCase() + "%";
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likeSearch));
        }
    }

    private static void filterByTechnologyEnabledField(Root<Technology> root, CriteriaBuilder criteriaBuilder,
                                                       Boolean enabled, List<Predicate> predicates) {
        if (enabled != null) {
            predicates.add(criteriaBuilder.equal(root.get("enabled"), enabled));
        }
    }
}
