package ru.otus.dto.filter;

import lombok.Builder;
import ru.otus.dto.page.PageDataFilter;

@Builder
public record StaffEvaluationUserFilter(
    Long staffEvaluationId,
    String search,
    Long projectRole
) implements PageDataFilter {
    @Override
    public String buildExtraQuery() {
        StringBuilder query = new StringBuilder();
        appendQueryParam(query, SEARCH, this.search());
        appendQueryParam(query, PROJECT_ROLE, this.projectRole());
        return query.toString();
    }
}