package ru.otus.services.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.otus.dto.AiEmployeeMatch;
import ru.otus.dto.AiEmployeeProfile;
import ru.otus.dto.AiEmployeeTechnology;
import ru.otus.dto.AiModelSearchResult;
import ru.otus.dto.AiProjectRoleCandidate;
import ru.otus.dto.AiQuestionCandidate;
import ru.otus.dto.AiTechnologyCandidate;
import ru.otus.dto.LatestAnswerProjection;
import ru.otus.entity.Question;
import ru.otus.entity.User;
import ru.otus.entity.UserTechnology;
import ru.otus.repositories.ProjectRoleRepository;
import ru.otus.repositories.QuestionRepository;
import ru.otus.repositories.StaffEvaluationAnswerRepository;
import ru.otus.repositories.TechnologyRepository;
import ru.otus.repositories.UserRepository;
import ru.otus.repositories.UserTechnologyRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.entity.enums.UserRole.USER;

@Service
@RequiredArgsConstructor
public class EmployeeSearchServiceImpl implements EmployeeSearchService {

    private static final int MAX_EMPLOYEE_MATCHES = 20;

    private static final int MAX_CONTEXT_TEXT_LENGTH = 10000;

    private static final int MAX_DESCRIPTION_SENTENCES = 10;

    private final ProjectRoleRepository projectRoleRepository;

    private final QuestionRepository questionRepository;

    private final StaffEvaluationAnswerRepository staffEvaluationAnswerRepository;

    private final UserRepository userRepository;

    private final TechnologyRepository technologyRepository;

    private final UserTechnologyRepository userTechnologyRepository;

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
    public List<AiTechnologyCandidate> findTechnologies() {
        return technologyRepository.findAllByOrderByNameAsc().stream()
            .filter(technology -> TRUE.equals(technology.getEnabled()))
            .map(technology -> AiTechnologyCandidate.builder()
                .id(technology.getId())
                .name(technology.getName())
                .build())
            .toList();
    }

    @Override
    public List<AiEmployeeMatch> findEmployees(
        Set<Long> questionIds,
        Set<Long> technologyIds,
        boolean matchAll
    ) {
        var answersByEmployeeMap = answersMapByEmployeeOf(questionIds);
        if (matchAll) {
            answersByEmployeeMap.entrySet()
                .removeIf(entry -> !matchesAll(entry.getValue(), questionIds));
        }
        var technologiesByEmployeeMap = technologiesMapByEmployeeOf(technologyIds);
        var candidateIds = candidateIdsOf(answersByEmployeeMap.keySet(), technologiesByEmployeeMap.keySet(),
            matchAll, isEmpty(questionIds));
        var employeeIds = employeeIdsOf(candidateIds);
        return candidateIds.stream()
            .filter(employeeIds::contains)
            .map(employeeId -> employeeMatchOf(
                employeeId,
                answersByEmployeeMap.getOrDefault(employeeId, Set.of()),
                technologiesByEmployeeMap.getOrDefault(employeeId, Set.of())
            ))
            .sorted(employeeMatchComparator())
            .limit(MAX_EMPLOYEE_MATCHES)
            .toList();
    }

    private Map<Long, Set<LatestAnswerProjection>> answersMapByEmployeeOf(Set<Long> questionIds) {
        var answers = isNotEmpty(questionIds) ?
            staffEvaluationAnswerRepository.findLatestPositiveAnswers(questionIds) :
            List.<LatestAnswerProjection>of();
        return answers.stream()
            .collect(
                groupingBy(
                    LatestAnswerProjection::userId,
                    LinkedHashMap::new,
                    toSet()
                )
            );
    }

    private Map<Long, Set<UserTechnology>> technologiesMapByEmployeeOf(Set<Long> technologyIds) {
        var technologies = isNotEmpty(technologyIds) ?
            userTechnologyRepository.findByTechnologyIdInAndTechnologyEnabledTrue(technologyIds) :
            List.<UserTechnology>of();
        return technologies.stream()
            .collect(groupingBy(
                technology -> technology.getUser().getId(),
                LinkedHashMap::new,
                toSet())
            );
    }

    private static Set<Long> candidateIdsOf(
        Set<Long> assessedEmployeeIds,
        Set<Long> technologyEmployeeIds,
        boolean matchAll,
        boolean questionsEmpty
    ) {
        var candidateIds = new LinkedHashSet<>(assessedEmployeeIds);
        if (!matchAll || questionsEmpty) {
            candidateIds.addAll(technologyEmployeeIds);
        }
        return candidateIds;
    }

    private Set<Long> employeeIdsOf(Set<Long> candidateIds) {
        return userRepository.findAllById(candidateIds).stream()
            .filter(user -> isNotEmpty(user.getRoles()) && user.getRoles().contains(USER))
            .map(User::getId)
            .collect(toSet());
    }

    @Override
    public AiEmployeeProfile findEmployeeByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return userRepository.findByUsername(username.trim())
            .filter(user -> user.getRoles() != null && user.getRoles().contains(USER))
            .map(user -> AiEmployeeProfile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .confirmedAnswers(staffEvaluationAnswerRepository.findLatestPositiveAnswersByUserId(user.getId())
                    .stream()
                    .map(LatestAnswerProjection::questionText)
                    .map(EmployeeSearchServiceImpl::abbreviate)
                    .toList())
                .selfReportedTechnologies(userTechnologyRepository.findByUserIdOrderByTechnologyNameAsc(user.getId())
                    .stream()
                    .filter(technology -> TRUE.equals(technology.getTechnology().getEnabled()))
                    .map(this::employeeTechnologyOf)
                    .toList())
                .build())
            .orElse(null);
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
            .forEach(selection -> appendEmployee(result, usersById.get(selection.id()), selection));
        return result.isEmpty() ? "Подходящие сотрудники не найдены." : result.toString();
    }

    private static void appendEmployee(
        StringBuilder result,
        User employee,
        AiModelSearchResult.AiModelEmployeeSelection selection
    ) {
        if (!result.isEmpty()) {
            result.append(System.lineSeparator());
        }
        result.append("- ").append(employee.getDisplayName())
            .append(" (@").append(employee.getUsername()).append(")");
        if (selection.description() != null && !selection.description().isBlank()) {
            result.append(" — ").append(limitSentences(selection.description().trim()));
        }
    }

    private static String questionInfo(Question question) {
        return String.join(" / ",
            question.getAreaKnowledge(),
            question.getSection(),
            question.getSkill() != null ? question.getSkill().getName() : EMPTY,
            question.getText()
        );
    }

    private static boolean matchesAll(Set<LatestAnswerProjection> answers, Set<Long> questionIds) {
        var matchedIds = answers.stream()
            .map(LatestAnswerProjection::questionId)
            .collect(toSet());
        return matchedIds.containsAll(questionIds);
    }

    private AiEmployeeMatch employeeMatchOf(
        Long employeeId,
        Set<LatestAnswerProjection> answers,
        Set<UserTechnology> technologies
    ) {
        return AiEmployeeMatch.builder()
            .id(employeeId)
            .confirmedAnswers(
                answers.stream()
                    .map(LatestAnswerProjection::questionText)
                    .map(EmployeeSearchServiceImpl::abbreviate)
                    .toList())
            .selfReportedTechnologies(technologies.stream()
                .map(this::employeeTechnologyOf)
                .toList())
            .build();
    }

    private AiEmployeeTechnology employeeTechnologyOf(UserTechnology technology) {
        var level = technology.getLevel();
        return AiEmployeeTechnology.builder()
            .name(technology.getTechnology().getName())
            .level(messageSource.getMessage(
                "TechnologyLevel.%s".formatted(level.name()),
                new Object[]{},
                level.name(),
                getLocale()))
            .levelOrder(level.getOrder())
            .build();
    }

    private static Comparator<AiEmployeeMatch> employeeMatchComparator() {
        return Comparator
            .comparingInt((AiEmployeeMatch employee) -> isNotEmpty(employee.confirmedAnswers()) ? 0 : 1)
            .thenComparing(Comparator.comparingInt(
                (AiEmployeeMatch employee) -> employee.confirmedAnswers().size()).reversed())
            .thenComparing(Comparator.comparingInt(
                (AiEmployeeMatch employee) -> employee.selfReportedTechnologies().size()).reversed())
            .thenComparing(Comparator.comparingInt(EmployeeSearchServiceImpl::technologyLevelScore).reversed())
            .thenComparing(AiEmployeeMatch::id);
    }

    private static int technologyLevelScore(AiEmployeeMatch employee) {
        return employee.selfReportedTechnologies().stream()
            .mapToInt(AiEmployeeTechnology::levelOrder)
            .sum();
    }

    private static String limitSentences(String value) {
        var boundaryCount = 0;
        for (var index = 0; index < value.length(); index++) {
            var character = value.charAt(index);
            if (character == '.' || character == '!' || character == '?') {
                boundaryCount++;
                if (boundaryCount == MAX_DESCRIPTION_SENTENCES) {
                    return value.substring(0, index + 1);
                }
            }
        }
        return value;
    }

    private static String abbreviate(String value) {
        if (value.length() <= MAX_CONTEXT_TEXT_LENGTH) {
            return value;
        } else {
            return value.substring(0, MAX_CONTEXT_TEXT_LENGTH - 1) + "…";
        }
    }
}
