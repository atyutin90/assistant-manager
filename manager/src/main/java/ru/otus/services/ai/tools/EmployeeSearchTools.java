package ru.otus.services.ai.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import ru.otus.dto.AiEmployeeMatch;
import ru.otus.dto.AiProjectRoleCandidate;
import ru.otus.dto.AiQuestionCandidate;
import ru.otus.services.ai.EmployeeSearchService;

import java.util.List;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
@RequiredArgsConstructor
public class EmployeeSearchTools {

    private final EmployeeSearchService employeeSearchService;

    @Tool(description = """
        Return all existing enabled project roles. Always call this tool before findQuestionsByProjectRoles.
        Choose only roles that fit the user's employee request. If no role can be determined, use an empty list.
        """)
    public List<AiProjectRoleCandidate> findProjectRoles() {
        return employeeSearchService.findProjectRoles();
    }

    @Tool(description = """
        Return all questionnaire questions for the selected project roles. Call it after findProjectRoles.
        The model itself must select question IDs whose meaning matches the user's requirements.
        Pass only role IDs returned by findProjectRoles, or an empty list when the role is unknown.
        """)
    public List<AiQuestionCandidate> findQuestionsByProjectRoles(
        @ToolParam(description = "Suitable project role IDs from findProjectRoles; empty if role is unknown")
        Set<Long> projectRoleIds
    ) {
        return employeeSearchService.findQuestionsByProjectRoles(projectRoleIds);
    }

    @Tool(description = """
        Return anonymous employee matches. Results contain employee IDs, project roles and verified positive facts,
        but never names or logins. Use only question IDs selected from findQuestionsByProjectRoles.
        Set matchAll=true when every selected question is mandatory, otherwise false.
        """)
    public List<AiEmployeeMatch> findEmployees(
        @ToolParam(description = "Relevant question IDs selected from findQuestionsByProjectRoles")
        Set<Long> questionIds,
        @ToolParam(description = "True if the employee must match every question; false to match any question")
        boolean matchAll
    ) {
        var safeQuestionIds = isNotEmpty(questionIds) ? questionIds : Set.<Long>of();
        return employeeSearchService.findEmployees(safeQuestionIds, matchAll);
    }
}
