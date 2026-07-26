package ru.otus.dto.filter;

import lombok.Builder;
import ru.otus.dto.page.PageDataFilter;

@Builder
public record QuestionFilter(
    String search,
    Long projectRole,
    Long skill,
    Boolean enabled
) implements PageDataFilter {

    @Override
    public String buildExtraQuery() {
        StringBuilder query = new StringBuilder();
        appendQueryParam(query, SEARCH, this.search());
        appendQueryParam(query, PROJECT_ROLE, this.projectRole());
        appendQueryParam(query, SKILL, this.skill());
        appendQueryParam(query, ENABLED, this.enabled());
        return query.toString();
    }
}