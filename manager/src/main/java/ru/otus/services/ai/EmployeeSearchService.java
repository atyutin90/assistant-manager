package ru.otus.services.ai;

import ru.otus.dto.AiEmployeeMatch;
import ru.otus.dto.AiModelSearchResult;
import ru.otus.dto.AiProjectRoleCandidate;
import ru.otus.dto.AiQuestionCandidate;

import java.util.List;
import java.util.Set;

public interface EmployeeSearchService {

    List<AiProjectRoleCandidate> findProjectRoles();

    List<AiQuestionCandidate> findQuestionsByProjectRoles(Set<Long> projectRoleIds);

    List<AiEmployeeMatch> findEmployees(Set<Long> questionIds, boolean matchAll);

    String resolveEmployeeData(AiModelSearchResult modelResult);
}
