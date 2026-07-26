package ru.otus.dto.filter;

import lombok.Builder;
import ru.otus.dto.page.PageDataFilter;

@Builder
public record StaffEvaluationQuestionFilter(
    String search,
    Long projectRole,
    Long skill,
    Long staffEvaluationId
) implements PageDataFilter {

    @Override
    public String buildExtraQuery() {
        StringBuilder query = new StringBuilder();
        appendQueryParam(query, SEARCH, search);
        appendQueryParam(query, PROJECT_ROLE, projectRole);
        appendQueryParam(query, SKILL, skill);
        return query.toString();
    }
}
