package ru.otus.services.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.otus.dto.AiEmployeeMatch;
import ru.otus.dto.AiModelSearchResult;
import ru.otus.dto.AiProjectRoleCandidate;
import ru.otus.dto.AiQuestionCandidate;
import ru.otus.dto.LatestAnswerProjection;
import ru.otus.entity.Question;
import ru.otus.entity.User;
import ru.otus.repositories.ProjectRoleRepository;
import ru.otus.repositories.QuestionRepository;
import ru.otus.repositories.StaffEvaluationAnswerRepository;
import ru.otus.repositories.UserRepository;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Service
@RequiredArgsConstructor
public class EmployeeSearchServiceImpl implements EmployeeSearchService {

    private static final int MAX_EMPLOYEE_MATCHES = 100;

    private static final int MAX_CONTEXT_TEXT_LENGTH = 50000;

    private final ProjectRoleRepository projectRoleRepository;

    private final QuestionRepository questionRepository;

    private final StaffEvaluationAnswerRepository staffEvaluationAnswerRepository;

    private final UserRepository userRepository;

    private final MessageSource messageSource;

    @Override
    public List<AiProjectRoleCandidate> findProjectRoles() {
        return projectRoleRepository.findAllByOrderByPositionAsc().stream()
            .filter(role -> TRUE.equals(role.getEnabled()))
            .map(role ->
                AiProjectRoleCandidate.builder()
                    .id(role.getId())
                    .name(role.getName())
                    .build())
            .toList();
    }

    @Override
    public List<AiQuestionCandidate> findQuestionsByProjectRoles(List<Long> projectRoleIds) {
        if (isNotEmpty(projectRoleIds)) {
            return questionRepository.findAllByProjectRoleIdInOrderByIdAsc(projectRoleIds).stream()
                .map(question ->
                    AiQuestionCandidate.builder()
                        .id(question.getId())
                        .text(questionInfo(question))
                        .build())
                .toList();
        } else {
            return List.of();
        }
    }

    @Override
    public List<AiEmployeeMatch> findEmployees(List<Long> questionIds, boolean matchAll) {
        var uniqueQuestionIds = isNotEmpty(questionIds) ? questionIds : List.<Long>of();
        var employeeAnswers = staffEvaluationAnswerRepository.findLatestPositiveAnswers(uniqueQuestionIds);
        var answersByEmployeeMap = employeeAnswers.stream()
            .collect(groupingBy(
                LatestAnswerProjection::userId,
                LinkedHashMap::new,
                toList()
            ));
        return answersByEmployeeMap.values().stream()
            .filter(answers -> !matchAll || matchesAll(answers, uniqueQuestionIds))
            .limit(MAX_EMPLOYEE_MATCHES)
            .map(EmployeeSearchServiceImpl::employeeMatchOf)
            .toList();
    }

    @Override
    public String resolveEmployeeData(AiModelSearchResult modelResult) {
        if (modelResult == null) {
            return "Не удалось получить результат поиска.";
        }
        var selections = modelResult.employees() == null
            ? List.<AiModelSearchResult.AiModelEmployeeSelection>of()
            : modelResult.employees();
        var selectedIds = selections.stream()
            .map(AiModelSearchResult.AiModelEmployeeSelection::id)
            .filter(java.util.Objects::nonNull)
            .collect(toCollection(LinkedHashSet::new));
        var usersById = userRepository.findAllById(selectedIds).stream()
            .collect(toMap(User::getId, user -> user));

        var result = new StringBuilder();
        if (modelResult.answer() != null && !modelResult.answer().isBlank()) {
            result.append(modelResult.answer().trim());
        }
        selections.stream()
            .filter(selection -> selection.id() != null && selectedIds.contains(selection.id()))
            .filter(selection -> usersById.containsKey(selection.id()))
            .forEach(selection -> appendEmployee(result, usersById.get(selection.id()).getDisplayName(), selection));
        return result.isEmpty() ? "Подходящие сотрудники не найдены." : result.toString();
    }

    private static void appendEmployee(
        StringBuilder result,
        String displayName,
        AiModelSearchResult.AiModelEmployeeSelection selection
    ) {
        if (!result.isEmpty()) {
            result.append(System.lineSeparator());
        }
        result.append("- ").append(displayName);
        if (selection.description() != null && !selection.description().isBlank()) {
            result.append(" — ").append(selection.description().trim());
        }
    }

    private static String questionInfo(Question question) {
        return String.join(" / ",
            question.getAreaKnowledge(),
            question.getSection(),
            question.getSkill() != null ? question.getSkill().getName() : "",
            question.getText()
        );
    }

    private static boolean hasAnyRole(Question question, Set<Long> roleIds) {
        return question.getProjectRole() != null && roleIds.contains(question.getProjectRole().getId());
    }

    private static boolean matchesAll(List<LatestAnswerProjection> answers, List<Long> questionIds) {
        var matchedIds = answers.stream()
            .map(LatestAnswerProjection::questionId)
            .collect(toSet());
        return matchedIds.containsAll(questionIds);
    }

    private static AiEmployeeMatch employeeMatchOf(List<LatestAnswerProjection> answers) {
        var employee = isNotEmpty(answers) ? answers.getFirst() : null;
        return employee != null ? AiEmployeeMatch.builder()
            .id(employee.userId())
            .projectRoleId(employee.projectRoleId())
            .confirmedAnswers(
                answers.stream()
                    .map(LatestAnswerProjection::questionText)
                    .map(EmployeeSearchServiceImpl::abbreviate)
                    .toList())
            .build() : null;
    }

    private static String abbreviate(String value) {
        if (value.length() <= MAX_CONTEXT_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_CONTEXT_TEXT_LENGTH - 1) + "…";
    }
}
