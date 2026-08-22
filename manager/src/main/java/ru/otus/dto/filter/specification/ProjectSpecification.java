package ru.otus.dto.filter.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.JoinType;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import ru.otus.dto.filter.ProjectFilter;
import ru.otus.entity.AssessmentProject;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Value
public class ProjectSpecification {

    public static Specification<AssessmentProject> projectFilterSpecification(
        ProjectFilter filter
    ) {
        return (root, query, cBuilder) -> {
            query.distinct(true);
            Predicate accessible = accessiblePredicate(filter, root, cBuilder);
            if (filter.active() != null) {
                Predicate predicate = cBuilder.and(cBuilder.equal(root.get("active"), filter.active()));
                accessible = cBuilder.and(accessible, predicate);
            }
            if (isBlank(filter.search())) {
                return accessible;
            }
            var search = filter.search().trim();
            var likeSearch = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cBuilder.like(cBuilder.lower(root.get("name")), likeSearch));
            predicates.add(cBuilder.like(cBuilder.lower(root.get("description")), likeSearch));
            return cBuilder.and(accessible, cBuilder.or(predicates.toArray(Predicate[]::new)));
        };
    }

    private static Predicate accessiblePredicate(ProjectFilter filter, Root<AssessmentProject> root,
                                                 CriteriaBuilder cBuilder) {
        var access = root.join("accesses", JoinType.LEFT);
        return cBuilder.or(
            cBuilder.equal(root.join("owner").get("id"), filter.managerId()),
            cBuilder.and(
                cBuilder.equal(access.get("manager").get("id"), filter.managerId()),
                cBuilder.or(
                    cBuilder.isTrue(access.get("readAccess")),
                    cBuilder.isTrue(access.get("editAccess"))
                )
            )
        );
    }
}
