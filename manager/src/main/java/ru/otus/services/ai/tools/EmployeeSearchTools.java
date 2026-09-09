package ru.otus.services.ai.tools;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import ru.otus.dto.AiEmployeeMatch;
import ru.otus.dto.AiEmployeeProfile;
import ru.otus.dto.AiQuestionCandidate;
import ru.otus.dto.AiTechnologyCandidate;
import ru.otus.services.ai.EmployeeSearchService;

import java.util.List;
import java.util.Set;

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
        log.debug("Finding questions by project roles by employee request with projectRoleIds: %s"
            .formatted(projectRoleIds));
        return employeeSearchService.findQuestionsByProjectRoles(projectRoleIds);
    }

    @Tool(description = """
        Return the catalog of technologies that employees can add to their profiles.
        These technologies and proficiency levels are self-reported and are not verified assessment facts.
        """)
    public List<AiTechnologyCandidate> findTechnologies() {
        log.debug("Finding technology catalog by employee request");
        return employeeSearchService.findTechnologies();
    }

    @Tool(description = """
        Return anonymous employee matches. Results contain employee IDs, project roles, verified positive assessment
        facts and separately marked self-reported technologies, but never names or logins. Use only question IDs
        selected from findQuestionsByProjectRoles and technology IDs selected from findTechnologies.
        Verified assessment facts always have priority over self-reported technologies.
        Set matchAll=true when every selected verified assessment question is mandatory, otherwise false.
        """)
    public List<AiEmployeeMatch> findEmployees(
        @ToolParam(description = "Relevant question IDs selected from findQuestionsByProjectRoles")
        Set<Long> questionIds,
        @ToolParam(description = "Relevant technology IDs selected from findTechnologies")
        Set<Long> technologyIds,
        @ToolParam(description = "True if the employee must match every question; false to match any question")
        boolean matchAll
    ) {
        log.debug("Find employees by request with questions: %s, technologies: %s, match all: %s"
            .formatted(questionIds, technologyIds, matchAll));
        var safeQuestionIds = isNotEmpty(questionIds) ? questionIds : Set.<Long>of();
        var safeTechnologyIds = isNotEmpty(technologyIds) ? technologyIds : Set.<Long>of();
        return employeeSearchService.findEmployees(safeQuestionIds, safeTechnologyIds, matchAll);
    }

    @Tool(description = """
        Find one employee by exact username and return a profile containing verified positive assessment facts and
        separately marked self-reported technologies with proficiency levels. Use this tool only when the user asks
        about a particular username. Never treat self-reported technologies as verified facts.
        """)
    public AiEmployeeProfile findEmployeeByUsername(
        @ToolParam(description = "Exact employee username without @") String username
    ) {
        log.debug("Finding employee profile by username: %s".formatted(username));
        return employeeSearchService.findEmployeeByUsername(username);
    }
}
