package ru.otus.services.ai;

import ru.otus.dto.AiEmployeeMatch;
import ru.otus.dto.AiEmployeeProfile;
import ru.otus.dto.AiModelSearchResult;
import ru.otus.dto.AiProjectRoleCandidate;
import ru.otus.dto.AiQuestionCandidate;
import ru.otus.dto.AiTechnologyCandidate;

import java.util.List;
import java.util.Set;

public interface EmployeeSearchService {

    List<AiProjectRoleCandidate> findProjectRoles();

    List<AiQuestionCandidate> findQuestionsByProjectRoles(List<Long> projectRoleIds);

    List<AiTechnologyCandidate> findTechnologies();

    List<AiEmployeeMatch> findEmployees(Set<Long> questionIds, Set<Long> technologyIds, boolean matchAll);

    AiEmployeeProfile findEmployeeByUsername(String username);

    String resolveEmployeeData(AiModelSearchResult modelResult);
}
