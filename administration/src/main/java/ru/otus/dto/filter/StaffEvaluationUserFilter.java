package ru.otus.dto.filter;

import lombok.Builder;
import ru.otus.dto.page.PageDataFilter;
import ru.otus.entity.enums.StaffEvaluationUserStatus;

@Builder
public record StaffEvaluationUserFilter(
    Long staffEvaluationId,
    String search,
    Long projectRole,
    StaffEvaluationUserStatus status
) implements PageDataFilter {
    @Override
    public String buildExtraQuery() {
        StringBuilder query = new StringBuilder();
        appendQueryParam(query, SEARCH, this.search());
        appendQueryParam(query, PROJECT_ROLE, this.projectRole());
        appendQueryParam(query, STATUS, this.status());
        return query.toString();
    }
}
