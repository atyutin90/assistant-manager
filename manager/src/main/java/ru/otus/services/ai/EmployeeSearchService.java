package ru.otus.services.ai;

import ru.otus.dto.AiEmployeeMatch;
import ru.otus.dto.AiModelSearchResult;
import ru.otus.dto.AiProjectRoleCandidate;
import ru.otus.dto.AiQuestionCandidate;

import java.util.List;

public interface EmployeeSearchService {

    List<AiProjectRoleCandidate> findProjectRoles();

    List<AiQuestionCandidate> findQuestionsByProjectRoles(List<Long> projectRoleIds);

    List<AiEmployeeMatch> findEmployees(List<Long> questionIds, boolean matchAll);

    String resolveEmployeeData(AiModelSearchResult modelResult);
}
