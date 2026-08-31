package ru.otus.services.employee;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.otus.dto.ManagerEmployeeAnswerDto;
import ru.otus.dto.ManagerEmployeeDetailsDto;
import ru.otus.dto.ManagerCareerLevelAnswersDto;
import ru.otus.dto.ManagerSkillAnswersDto;
import ru.otus.dto.ManagerSkillLevelDto;
import ru.otus.dto.ManagerEmployeeDto;
import ru.otus.dto.filter.UserFilter;
import ru.otus.dto.filter.ManagerUserFilter;
import ru.otus.entity.AssessmentProject;
import ru.otus.entity.AssessmentProjectQuestion;
import ru.otus.entity.CareerLevel;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.Skill;
import ru.otus.entity.StaffEvaluationAnswer;
import ru.otus.entity.StaffEvaluationQuestion;
import ru.otus.entity.StaffEvaluationUser;
import ru.otus.entity.User;
import ru.otus.entity.enums.AnswerResponse;
import ru.otus.repositories.AssessmentProjectQuestionRepository;
import ru.otus.repositories.AssessmentProjectRepository;
import ru.otus.repositories.CareerLevelRepository;
import ru.otus.repositories.SkillRepository;
import ru.otus.repositories.StaffEvaluationAnswerRepository;
import ru.otus.repositories.StaffEvaluationQuestionRepository;
import ru.otus.repositories.StaffEvaluationUserRepository;
import ru.otus.repositories.UserRepository;
import ru.otus.services.ProjectRoleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.lang.Boolean.TRUE;
import static java.lang.Integer.MAX_VALUE;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static ru.otus.dto.filter.specification.UserSpecification.userFilterSpecification;
import static ru.otus.entity.enums.AnswerResponse.NO;
import static ru.otus.entity.enums.AnswerResponse.YES;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.COMPLETED;
import static ru.otus.entity.enums.UserRole.USER;
import static ru.otus.exceptions.WebApplicationException.errorOf;
import static ru.otus.utils.CareerLevelCalculator.calculateCurrentCareerLevel;

@Service
@RequiredArgsConstructor
public class ManagerEmployeeServiceImpl implements ManagerEmployeeService {

    private final UserRepository userRepository;

    private final AssessmentProjectRepository projectRepository;

    private final AssessmentProjectQuestionRepository projectQuestionRepository;

    private final SkillRepository skillRepository;

    private final CareerLevelRepository careerLevelRepository;

    private final ProjectRoleService projectRoleService;

    private final StaffEvaluationUserRepository staffEvaluationUserRepository;

    private final StaffEvaluationAnswerRepository staffEvaluationAnswerRepository;

    private final StaffEvaluationQuestionRepository staffEvaluationQuestionRepository;

    private final MessageSource messageSource;

    public List<ManagerEmployeeDto> findAll(ManagerUserFilter filter) {
        var context = assessmentProjectContextOf(filter);

        var userFilter = UserFilter.builder()
            .search(filter.search())
            .projectRole(filter.projectRole())
            .role(USER)
            .build();

        //TODO: Fetching all users isn't the best approach in terms of performance,
        // we should think about a better way to handle this in the future.
        var employees = userRepository.findAll(userFilterSpecification(userFilter));

        return managerEmployeeDtoListOf(filter, employees, context);
    }

    @Override
    public ManagerEmployeeDetailsDto findDetails(Long employeeId,
                                                 Long staffEvaluationId,
                                                 Long projectId,
                                                 Long managerId,
                                                 Long projectRoleId) {
        checkRole(projectRoleId);
        var accessibleProjectId = accessibleProjectIdOf(projectId, managerId);
        var projectQuestions = accessibleProjectId != null ?
            projectQuestionRepository.findByProjectId(accessibleProjectId) :
            List.<AssessmentProjectQuestion>of();
        var staffEvaluationUser = staffEvaluationUserOf(staffEvaluationId, employeeId, projectRoleId);
        var answers = answersOf(staffEvaluationUser);
        var projectAnswers = projectAnswersOf(projectQuestions, answers);
        var skillGroups = skillGroupsOf(projectQuestions, projectAnswers);
        return detailsOf(staffEvaluationUser, projectAnswers, skillGroups, accessibleProjectId);
    }

    private void checkRole(Long projectRoleId) {
        projectRoleService.findById(projectRoleId);
    }

    private Long accessibleProjectIdOf(Long projectId, Long managerId) {
        return accessibleProjectIdOf(
            ManagerUserFilter.builder()
                .project(projectId)
                .managerId(managerId)
                .build()
        );
    }

    private ManagerEmployeeDetailsDto detailsOf(StaffEvaluationUser staffEvaluationUser,
                                                List<StaffEvaluationAnswer> answers,
                                                List<ManagerSkillAnswersDto> skillGroups,
                                                Long projectId) {
        var staffEvaluation = staffEvaluationUser.getStaffEvaluation();
        var employee = staffEvaluationUser.getUser();
        return ManagerEmployeeDetailsDto.builder()
            .employeeId(employee.getId())
            .fullName(employee.getDisplayName())
            .username(employee.getUsername())
            .email(employee.getEmail())
            .projectRole(ofNullable(staffEvaluationUser.getProjectRole()).map(ProjectRole::getName).orElse(null))
            .projectRoleId(ofNullable(staffEvaluationUser.getProjectRole()).map(ProjectRole::getId).orElse(null))
            .projectId(projectId)
            .staffEvaluationId(staffEvaluation.getId())
            .verifiedBy(verifiedByOf(staffEvaluationUser))
            .feedback(staffEvaluationUser.getFeedbackMessage())
            .answerCount(answers.size())
            .matchedAnswerCount(countVerifiedAnswers(answers, YES))
            .mismatchedAnswerCount(countVerifiedAnswers(answers, NO))
            .skills(skillGroups)
            .build();
    }

    private StaffEvaluationUser staffEvaluationUserOf(Long staffEvaluationId, Long employeeId, Long projectRoleId) {
        if (isNull(staffEvaluationId)) {
            return staffEvaluationUserRepository
                .findLastByStatusForUserIdAndProjectRoleId(employeeId, projectRoleId, COMPLETED)
                .orElseThrow(() -> errorOf(
                    NOT_FOUND,
                    messageSource.getMessage("error.staff-evaluation-not-found", new Object[]{}, getLocale()))
                );
        } else {
            return staffEvaluationUserRepository
                .findByStaffEvaluationIdAndUserIdAndProjectRoleId(staffEvaluationId, employeeId, projectRoleId)
                .orElseThrow(() -> errorOf(
                    NOT_FOUND,
                    messageSource.getMessage("error.staff-evaluation-not-found", new Object[]{}, getLocale()))
                );
        }
    }

    private List<StaffEvaluationAnswer> answersOf(StaffEvaluationUser staffEvaluationUser) {
        var positions = questionPositionMapOf(staffEvaluationUser).entrySet().stream()
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return staffEvaluationAnswerRepository.findByStaffEvaluationUserId(staffEvaluationUser.getId()).stream()
            .sorted(comparingInt(answer -> positions.getOrDefault(answer.getQuestion().getId(), MAX_VALUE)))
            .toList();
    }

    private static List<StaffEvaluationAnswer> projectAnswersOf(
        List<AssessmentProjectQuestion> projectQuestions,
        List<StaffEvaluationAnswer> answers
    ) {
        var questionIds = projectQuestions.stream()
            .map(value -> value.getQuestion().getId())
            .collect(toSet());
        return answers.stream()
            .filter(answer -> questionIds.contains(answer.getQuestion().getId()))
            .toList();
    }

    private List<ManagerSkillAnswersDto> skillGroupsOf(List<AssessmentProjectQuestion> projectQuestions,
                                                       List<StaffEvaluationAnswer> projectAnswers) {
        var skillGroups = List.<ManagerSkillAnswersDto>of();
        if (isNotEmpty(projectQuestions)) {
            var projectQuestionMap = projectQuestions.stream()
                .collect(toMap(value -> value.getQuestion().getId(), identity()));
            Map<Skill, Map<CareerLevel, List<StaffEvaluationAnswer>>> groupedAnswersBySkill = new LinkedHashMap<>();
            projectAnswers.forEach(answer -> addToGroup(answer, projectQuestionMap, groupedAnswersBySkill));
            var yesQuestionIds = getYesQuestionIds(projectAnswers);
            var activeCareerLevels = activeCareerLevelsOf();

            skillGroups = activeSkillsOf().stream()
                .map(skill -> skillAnswersOf(skill, groupedAnswersBySkill.getOrDefault(skill, Map.of()),
                    projectQuestionMap, yesQuestionIds, activeCareerLevels))
                .toList();

        }
        return skillGroups;
    }

    private List<ManagerEmployeeDto> managerEmployeeDtoListOf(ManagerUserFilter filter,
                                                              List<User> employees,
                                                              AssessmentProjectContext context) {

        var staffEvaluationUserMap = completedEvaluationsOf(filter).stream()
            .collect(toMap(seu -> keyOf(seu.getUser(), seu.getProjectRole()), Function.identity()));

        return employees.stream()
            .flatMap(user ->
                user.getProjectRoles().stream()
                    .map(projectRole ->
                        ofNullable(staffEvaluationUserMap.get(keyOf(user, projectRole)))
                            .map(staffEvaluationUser ->
                                dtoOf(user, projectRole,
                                    calculateSkillLevelsForEmployee(user, staffEvaluationUser, context),
                                    staffEvaluationUser
                                ))
                            .orElseGet(() -> dtoOf(user, projectRole, defaultSkillLevelsForEmployee(context)))
                ))
            .filter(managerEmployeeDto -> matchesCareerLevel(managerEmployeeDto, filter))
            .filter(managerEmployeeDto -> matchesProjectRole(managerEmployeeDto, filter))
            .sorted(comparing(ManagerEmployeeDto::lastName))
            .toList();
    }

    private String keyOf(User user, ProjectRole projectRole) {
        return "%d:%d".formatted(user.getId(), projectRole.getId());
    }

    private List<StaffEvaluationUser> completedEvaluationsOf(ManagerUserFilter filter) {
        return filter.staffEvaluation() == null
            ? staffEvaluationUserRepository.findLastByStatusForAllUsers(COMPLETED)
            : staffEvaluationUserRepository.findByStaffEvaluationIdAndStatus(filter.staffEvaluation(), COMPLETED);
    }

    private static int countVerifiedAnswers(List<StaffEvaluationAnswer> answers, AnswerResponse response) {
        return (int) answers.stream().filter(answer -> response == answer.getVerifiedResponse()).count();
    }

    private static String verifiedByOf(StaffEvaluationUser assignment) {
        return assignment.getVerifiedBy() != null ? assignment.getVerifiedBy().getDisplayName() : EMPTY;
    }

    private AssessmentProjectContext assessmentProjectContextOf(ManagerUserFilter filter) {
        var projectId = accessibleProjectIdOf(filter);
        var assessmentProjectQuestions = projectId != null ?
            projectQuestionRepository.findByProjectId(projectId) :
            List.<AssessmentProjectQuestion>of();
        var questionMapByProjectRoleAndSkillAndCareerLevel = assessmentProjectQuestions.stream()
            .filter(it -> it.getQuestion().getEnabled())
            .collect(groupingBy(it ->
                        it.getQuestion().getProjectRole(),
                    collectingAndThen(toList(), this::groupingSkillAndCareerLevel)
                )
            );
        return AssessmentProjectContext.builder()
            .projectId(projectId)
            .activeSkills(activeSkillsOf())
            .projectQuestionMap(questionMapByProjectRoleAndSkillAndCareerLevel)
            .build();
    }

    private Map<Skill, Map<CareerLevel, List<AssessmentProjectQuestion>>> groupingSkillAndCareerLevel(
        List<AssessmentProjectQuestion> projectQuestions
    ) {
        return projectQuestions.stream()
            .filter(it -> it.getQuestion().getEnabled())
            .filter(it -> it.getQuestion().getSkill() != null)
            .collect(groupingBy(it ->
                        it.getQuestion().getSkill(),
                    collectingAndThen(toList(), this::groupingCareerLevel)
                )
            );
    }

    private Map<CareerLevel, List<AssessmentProjectQuestion>> groupingCareerLevel(
        List<AssessmentProjectQuestion> projectQuestions
    ) {
        return projectQuestions.stream()
            .filter(it -> it.getQuestion().getEnabled())
            .filter(it -> it.getCareerLevel() != null)
            .collect(groupingBy(AssessmentProjectQuestion::getCareerLevel));
    }

    private static List<ManagerSkillLevelDto> defaultSkillLevelsForEmployee(AssessmentProjectContext context) {
        return context.activeSkills.stream()
            .map(it -> ManagerSkillLevelDto.builder()
                .skillId(it.getId())
                .skill(it.getName())
                .build())
            .toList();
    }

    private List<ManagerSkillLevelDto> calculateSkillLevelsForEmployee(User user,
                                                                       StaffEvaluationUser staffEvaluationUser,
                                                                       AssessmentProjectContext context
    ) {
        var yesQuestionIds = getYesQuestionIds(staffEvaluationUser);
        var projectQuestion = context.projectQuestionMap
            .getOrDefault(staffEvaluationUser.getProjectRole(), new HashMap<>());
        return skillLevelsOf(projectQuestion, context.activeSkills(), yesQuestionIds);
    }

    private List<Skill> activeSkillsOf() {
        return skillRepository.findAllByOrderByPositionAsc().stream()
            .filter(skill -> TRUE.equals(skill.getEnabled()))
            .toList();
    }

    private ManagerSkillLevelDto skillLevelOf(Skill skill, CareerLevel careerLevel) {
        return ManagerSkillLevelDto.builder()
            .skillId(skill.getId())
            .skill(skill.getName())
            .careerLevelId(careerLevel == null ? null : careerLevel.getId())
            .careerLevel(careerLevel == null ? null : careerLevel.getName())
            .build();
    }

    private Long accessibleProjectIdOf(ManagerUserFilter filter) {
        Long projectId = null;
        if (filter != null && filter.project() != null) {
            projectId = projectRepository.findAccessibleById(filter.project(), filter.managerId())
                .map(AssessmentProject::getId)
                .orElse(null);
        }
        return projectId;
    }

    private boolean matchesCareerLevel(ManagerEmployeeDto user, ManagerUserFilter filter) {
        var result = true;
        if (filter.careerLevel() != null && filter.skill() != null) {
            result = user.skillLevels().stream()
                .filter(level -> filter.skill().equals(level.skillId()))
                .anyMatch(level -> filter.careerLevel().equals(level.careerLevelId()));
        } else if (filter.careerLevel() != null) {
            result = user.skillLevels().stream()
                .allMatch(level -> filter.careerLevel().equals(level.careerLevelId()));
        } else if (filter.skill() != null) {
            result = user.skillLevels().stream()
                .filter(level -> level.careerLevelId() != null)
                .anyMatch(level -> filter.skill().equals(level.skillId()));
        }
        return result;
    }

    private boolean matchesProjectRole(ManagerEmployeeDto user, ManagerUserFilter filter) {
        var result = true;
        if (filter.projectRole() != null) {
            result = user.projectRoleId().equals(filter.projectRole());
        }
        return result;
    }

    private ManagerEmployeeDto dtoOf(User user, ProjectRole projectRole, List<ManagerSkillLevelDto> skillLevels) {
        return dtoOf(user, projectRole, skillLevels, null);
    }

    private ManagerEmployeeDto dtoOf(User user, ProjectRole projectRole, List<ManagerSkillLevelDto> skillLevels,
                                     StaffEvaluationUser staffEvaluationUser) {
        return ManagerEmployeeDto.builder()
            .id(user.getId())
            .lastName(user.getLastName())
            .middleName(user.getMiddleName())
            .firstName(user.getFirstName())
            .username(user.getUsername())
            .email(user.getEmail())
            .projectRoleId(projectRole != null ? projectRole.getId() : null)
            .projectRole(projectRole != null ? projectRole.getName() : null)
            .projectRoleCode(projectRole != null ? projectRole.getCode() : null)
            .staffEvaluationId(staffEvaluationUser == null
                ? null
                : staffEvaluationUser.getStaffEvaluation().getId())
            .skillLevels(skillLevels)
            .build();
    }

    private Map<Long, Integer> questionPositionMapOf(StaffEvaluationUser staffEvaluationUser) {
        var projectRole = staffEvaluationUser.getProjectRole();
        if (projectRole == null) {
            return Map.of();
        }
        return staffEvaluationQuestionRepository
            .findByStaffEvaluationIdAndQuestionProjectRoleIdOrderByPositionAsc(
                staffEvaluationUser.getStaffEvaluation().getId(), projectRole.getId()).stream()
            .collect(toMap(
                question -> question.getQuestion().getId(),
                StaffEvaluationQuestion::getPosition)
            );
    }

    private ManagerSkillAnswersDto skillAnswersOf(
        Skill skill,
        Map<CareerLevel, List<StaffEvaluationAnswer>> answersByCareerLevelMap,
        Map<Long, AssessmentProjectQuestion> projectQuestionMap,
        Set<Long> yesQuestionIds,
        List<CareerLevel> activeCareerLevels
    ) {
        var questionsByCareerLevelMap = answersByCareerLevelMap.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                .map(answer -> projectQuestionMap.get(answer.getQuestion().getId()))
                .toList()));
        var calculatedLevel = calculateCurrentCareerLevel(questionsByCareerLevelMap, yesQuestionIds);
        return ManagerSkillAnswersDto.builder()
            .skillId(skill != null ? skill.getId() : null)
            .skill(skill != null ? skill.getName() : null)
            .answerCount(answersByCareerLevelMap.values().stream().mapToInt(List::size).sum())
            .calculatedCareerLevel(calculatedLevel != null ? calculatedLevel.getName() : null)
            .careerLevels(careerLevelGroupsOf(answersByCareerLevelMap, activeCareerLevels))
            .build();
    }

    private void addToGroup(
        StaffEvaluationAnswer answer,
        Map<Long, AssessmentProjectQuestion> projectQuestionMap,
        Map<Skill, Map<CareerLevel, List<StaffEvaluationAnswer>>> groupedAnswers
    ) {
        var projectQuestion = projectQuestionMap.get(answer.getQuestion().getId());
        if (projectQuestion != null) {
            groupedAnswers
                .computeIfAbsent(answer.getQuestion().getSkill(), ignored -> new LinkedHashMap<>())
                .computeIfAbsent(projectQuestion.getCareerLevel(), ignored -> new ArrayList<>())
                .add(answer);
        }
    }

    private List<ManagerCareerLevelAnswersDto> careerLevelGroupsOf(
        Map<CareerLevel, List<StaffEvaluationAnswer>> answersByCareerLevel,
        List<CareerLevel> activeCareerLevels
    ) {
        return activeCareerLevels.stream()
            .map(careerLevel -> ManagerCareerLevelAnswersDto.builder()
                .careerLevelId(careerLevel.getId())
                .careerLevel(careerLevel.getName())
                .answers(answersByCareerLevel.getOrDefault(careerLevel, List.of()).stream()
                    .map(this::answerOf).toList())
                .build())
            .toList();
    }

    private List<CareerLevel> activeCareerLevelsOf() {
        return careerLevelRepository.findAllByOrderByPositionAsc().stream()
            .filter(careerLevel -> TRUE.equals(careerLevel.getEnabled()))
            .toList();
    }

    private ManagerEmployeeAnswerDto answerOf(StaffEvaluationAnswer answer) {
        var question = answer.getQuestion();
        return ManagerEmployeeAnswerDto.builder()
            .areaKnowledge(question.getAreaKnowledge())
            .section(question.getSection())
            .question(question.getText())
            .response(answer.getResponse())
            .verifiedResponse(answer.getVerifiedResponse())
            .verificationComment(answer.getVerificationComment())
            .build();
    }

    private List<ManagerSkillLevelDto> skillLevelsOf(
        Map<Skill, Map<CareerLevel, List<AssessmentProjectQuestion>>> projectQuestionMapBySkill,
        List<Skill> activeSkills,
        Set<Long> yesQuestionIds
    ) {
        return activeSkills.stream()
            .map(skill -> {
                var projectMapByCareerLevel = projectQuestionMapBySkill.getOrDefault(skill, new HashMap<>());
                var resultCareerLevel = calculateCurrentCareerLevel(projectMapByCareerLevel, yesQuestionIds);
                return skillLevelOf(skill, resultCareerLevel);
            }).collect(toList());
    }

    private static Set<Long> getYesQuestionIds(StaffEvaluationUser staffEvaluationUser) {
        return getYesQuestionIds(staffEvaluationUser.getAnswers().stream().toList());
    }

    private static Set<Long> getYesQuestionIds(List<StaffEvaluationAnswer> answers) {
        return answers.stream()
            .filter(answer -> YES.equals(answer.getResponse()) && YES.equals(answer.getVerifiedResponse()))
            .map(answer -> answer.getQuestion().getId())
            .collect(toSet());
    }

    @Builder
    private record AssessmentProjectContext(
        Long projectId,
        List<Skill> activeSkills,
        Map<ProjectRole, Map<Skill, Map<CareerLevel, List<AssessmentProjectQuestion>>>> projectQuestionMap
    ) {
    }
}
