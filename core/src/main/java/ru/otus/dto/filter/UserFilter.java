package ru.otus.dto.filter;

import lombok.Builder;
import ru.otus.dto.page.PageDataFilter;
import ru.otus.entity.enums.UserRole;

@Builder
public record UserFilter(
    String search,
    Long projectRole,
    UserRole role
) implements PageDataFilter {

    @Override
    public String buildExtraQuery() {
        StringBuilder query = new StringBuilder();
        appendQueryParam(query, SEARCH, this.search());
        appendQueryParam(query, PROJECT_ROLE, this.projectRole());
        return query.toString();
    }

}