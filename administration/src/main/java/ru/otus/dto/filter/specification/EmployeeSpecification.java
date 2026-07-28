package ru.otus.dto.filter.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import ru.otus.dto.filter.StaffEvaluationUserFilter;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.StaffEvaluation;
import ru.otus.entity.User;
import ru.otus.entity.StaffEvaluationUser;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Value
public class EmployeeSpecification {

    public static Specification<StaffEvaluationUser> employeeFilterSpecification(StaffEvaluationUserFilter filter) {

        var search = filter.search();

        return (root, query, cbuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            filterByStaffEvaluation(filter, root, cbuilder, predicates);

            filterByStaffEvaluationUserField(root, cbuilder, search, predicates);

            filterByProjectRole(filter, root, cbuilder, predicates);

            return cbuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void filterByStaffEvaluation(StaffEvaluationUserFilter filter, Root<StaffEvaluationUser> root,
                                                CriteriaBuilder cbuilder, List<Predicate> predicates) {
        if (isNotEmpty(filter.staffEvaluationId())) {
            Join<StaffEvaluationUser, StaffEvaluation> staffEvaluationJoin = root.join("staffEvaluation");
            predicates.add(cbuilder.equal(staffEvaluationJoin.get("id"), filter.staffEvaluationId()));
        }
    }

    private static void filterByStaffEvaluationUserField(Root<StaffEvaluationUser> root, CriteriaBuilder cbuilder,
                                                         String search, List<Predicate> predicates) {
        if (StringUtils.hasText(search)) {
            String trimmedSearch = search.trim();
            String likeSearch = "%" + trimmedSearch.toLowerCase() + "%";
            List<Predicate> searchPredicates = new ArrayList<>();

            Join<StaffEvaluationUser, User> userJoin = root.join("user");
            searchPredicates.add(cbuilder.like(cbuilder.lower(userJoin.get("lastName")), likeSearch));
            searchPredicates.add(cbuilder.like(cbuilder.lower(userJoin.get("middleName")), likeSearch));
            searchPredicates.add(cbuilder.like(cbuilder.lower(userJoin.get("firstName")), likeSearch));
            searchPredicates.add(cbuilder.like(cbuilder.lower(userJoin.get("username")), likeSearch));

            predicates.add(cbuilder.or(searchPredicates.toArray(Predicate[]::new)));
        }
    }

    private static void filterByProjectRole(StaffEvaluationUserFilter filter, Root<StaffEvaluationUser> root,
                                            CriteriaBuilder cbuilder, List<Predicate> predicates) {
        if (isNotEmpty(filter.projectRole())) {
            Join<StaffEvaluationUser, User> userJoin = root.join("user");
            Join<User, ProjectRole> projectRoleJoin = userJoin.join("projectRole");
            predicates.add(cbuilder.equal(projectRoleJoin.get("id"), filter.projectRole()));
        }
    }
}
