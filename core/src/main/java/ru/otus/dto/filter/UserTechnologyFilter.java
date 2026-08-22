package ru.otus.dto.filter;

import lombok.Builder;
import ru.otus.dto.page.PageDataFilter;

@Builder
public record UserTechnologyFilter(
    Long userId,
    String search
) implements PageDataFilter {

    @Override
    public String buildExtraQuery() {
        StringBuilder query = new StringBuilder();
        appendQueryParam(query, SEARCH, this.search());
        return query.toString();
    }
}