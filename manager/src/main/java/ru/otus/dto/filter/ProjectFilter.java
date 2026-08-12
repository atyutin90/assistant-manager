package ru.otus.dto.filter;

import lombok.Builder;
import ru.otus.dto.page.PageDataFilter;

@Builder
public record ProjectFilter(
    Boolean active,
    String search,
    Long managerId
) implements PageDataFilter {

    @Override
    public String buildExtraQuery() {
        StringBuilder query = new StringBuilder();
        appendQueryParam(query, SEARCH, search);
        return query.toString();
    }
}
