package ru.otus.services.ai.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import ru.otus.dto.AiEmployeeMatch;
import ru.otus.dto.AiQuestionCandidate;
import ru.otus.services.ai.EmployeeSearchService;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeSearchTools {

    private final EmployeeSearchService employeeSearchService;

    @Tool(description = """
        Return all questionnaire questions for the selected project roles.
        The model itself must select question IDs whose meaning matches the user's requirements.
        Pass only role IDs from the project role catalog in the system prompt, or an empty list when the role is unknown.
        """)
    public List<AiQuestionCandidate> findQuestionsByProjectRoles(
        @ToolParam(description = "Suitable IDs from the project role catalog; empty if the role is unknown")
        List<Long> projectRoleIds
    ) {
        log.info("Finding questions by project roles by employee request. " + projectRoleIds);
        return employeeSearchService.findQuestionsByProjectRoles(projectRoleIds);
    }

    @Tool(description = """
        Return anonymous employee matches. Results contain employee IDs, project roles and verified positive facts,
        but never names or logins. Use only question IDs selected from findQuestionsByProjectRoles.
        Set matchAll=true when every selected question is mandatory, otherwise false.
        """)
    public List<AiEmployeeMatch> findEmployees(
        @ToolParam(description = "Relevant question IDs selected from findQuestionsByProjectRoles")
        List<Long> questionIds,
        @ToolParam(description = "True if the employee must match every question; false to match any question")
        boolean matchAll
    ) {
        log.info("Find employees by project roles by employee request. " + questionIds + " and " + matchAll);
        var safeQuestionIds = isNotEmpty(questionIds) ? questionIds : List.<Long>of();
        return employeeSearchService.findEmployees(safeQuestionIds, matchAll);
    }
}
