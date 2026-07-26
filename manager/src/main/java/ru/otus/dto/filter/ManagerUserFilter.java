package ru.otus.dto.filter;

import lombok.Builder;
import ru.otus.dto.page.PageDataFilter;

@Builder
public record ManagerUserFilter(
    String search,
    Long projectRole,
    Long careerLevel,
    Long skill,
    Long project,
    Long managerId
) implements PageDataFilter {

    @Override
    public String buildExtraQuery() {
        StringBuilder query = new StringBuilder();
        appendQueryParam(query, SEARCH, search);
        appendQueryParam(query, PROJECT_ROLE, projectRole);
        appendQueryParam(query, CAREER_LEVEL, careerLevel);
        appendQueryParam(query, SKILL, skill);
        appendQueryParam(query, PROJECT, project);
        return query.toString();
    }

    public static ManagerUserFilter of(EmployeeFilter employeeFilter, Long projectId, Long managerId) {
        return ManagerUserFilter.builder()
            .search(employeeFilter.search())
            .projectRole(employeeFilter.projectRole())
            .careerLevel(employeeFilter.careerLevel())
            .skill(employeeFilter.skill())
            .project(projectId)
            .managerId(managerId)
            .build();
    }

    @Builder
    public record EmployeeFilter(
        String search,
        Long projectRole,
        Long careerLevel,
        Long skill
    ) {}
}
