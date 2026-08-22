package ru.otus.dto.filter.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.otus.dto.filter.UserTechnologyFilter;
import ru.otus.entity.UserTechnology;

import java.util.ArrayList;
import java.util.List;

@Value
public class UserTechnologySpecification {

    public static Specification<UserTechnology> technologyFilterSpecification(UserTechnologyFilter filter) {

        var search = filter.search();

        var userId = filter.userId();

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            filterByTechnologyField(root, criteriaBuilder, search, predicates);

            filterByUserField(root, criteriaBuilder, userId, predicates);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void filterByUserField(Root<UserTechnology> root, CriteriaBuilder criteriaBuilder, Long userId,
                                          List<Predicate> predicates) {
        if (userId != null) {
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
        }
    }

    private static void filterByTechnologyField(Root<UserTechnology> root, CriteriaBuilder criteriaBuilder,
                                                String search, List<Predicate> predicates) {
        if (StringUtils.hasText(search)) {
            String trimmedSearch = search.trim();
            String likeSearch = "%" + trimmedSearch.toLowerCase() + "%";
            predicates.add(criteriaBuilder.like(
                criteriaBuilder.lower(root.get("technology").get("name")), likeSearch));
        }
    }
}
