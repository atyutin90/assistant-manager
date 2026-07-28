package ru.otus.dto.page;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public interface PageDataFilter {

    String SEARCH = "search";

    String PROJECT = "project";

    String PROJECT_ROLE = "projectRole";

    String SKILL = "skill";

    String CAREER_LEVEL = "careerLevel";

    String ENABLED = "enabled";

    String buildExtraQuery();

    default void appendQueryParam(StringBuilder query, String name, Object value) {
        if (value != null && isNotEmpty(value.toString())) {
            query.append('&')
                .append(name)
                .append('=')
                .append(encode(value.toString(), UTF_8));
        }
    }
}
