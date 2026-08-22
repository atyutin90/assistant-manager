package ru.otus.dto.filter.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.otus.dto.filter.UserFilter;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.User;
import ru.otus.entity.enums.UserRole;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Value
public class UserSpecification {

    public static Specification<User> userFilterSpecification(UserFilter filter) {

        var search = filter.search();

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            filterByUserField(root, criteriaBuilder, search, predicates);

            filterByProjectRole(filter, root, criteriaBuilder, predicates);

            filterByRole(filter, root, criteriaBuilder, predicates);

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void filterByUserField(Root<User> root, CriteriaBuilder criteriaBuilder, String search,
                                          List<Predicate> predicates) {
        if (StringUtils.hasText(search)) {
            String trimmedSearch = search.trim();
            String likeSearch = "%" + trimmedSearch.toLowerCase() + "%";
            List<Predicate> searchPredicates = new ArrayList<>();

            searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likeSearch));
            searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("middleName")), likeSearch));
            searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likeSearch));
            searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likeSearch));

            predicates.add(criteriaBuilder.or(searchPredicates.toArray(Predicate[]::new)));
        }
    }

    private static void filterByProjectRole(UserFilter filter, Root<User> root, CriteriaBuilder criteriaBuilder,
                                            List<Predicate> predicates) {
        if (isNotEmpty(filter.projectRole())) {
            Join<User, ProjectRole> projectRoleJoin = root.join("projectRole");
            predicates.add(criteriaBuilder.equal(projectRoleJoin.get("id"), filter.projectRole()));
        }
    }

    private static void filterByRole(UserFilter filter, Root<User> root, CriteriaBuilder criteriaBuilder,
                                     List<Predicate> predicates) {
        if (filter.role() != null) {
            Join<User, UserRole> rolesJoin = root.join("roles");
            predicates.add(criteriaBuilder.equal(rolesJoin, filter.role()));
        }
    }
}
