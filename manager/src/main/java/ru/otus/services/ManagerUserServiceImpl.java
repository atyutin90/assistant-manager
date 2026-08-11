package ru.otus.services;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.dto.ManagerSkillLevelDto;
import ru.otus.dto.ManagerUserDto;
import ru.otus.dto.filter.UserFilter;
import ru.otus.dto.filter.ManagerUserFilter;
import ru.otus.entity.AssessmentProject;
import ru.otus.entity.AssessmentProjectQuestion;
import ru.otus.entity.CareerLevel;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.Skill;
import ru.otus.entity.StaffEvaluationUser;
import ru.otus.entity.User;
import ru.otus.repositories.AssessmentProjectQuestionRepository;
import ru.otus.repositories.AssessmentProjectRepository;
import ru.otus.repositories.SkillRepository;
import ru.otus.repositories.StaffEvaluationUserRepository;
import ru.otus.repositories.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static ru.otus.dto.filter.specification.UserSpecification.userFilterSpecification;
import static ru.otus.entity.enums.AnswerResponse.YES;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.COMPLETED;
import static ru.otus.entity.enums.UserRole.USER;
import static ru.otus.utils.CareerLevelCalculator.calculateCurrentCareerLevel;

@Service
@RequiredArgsConstructor
public class ManagerUserServiceImpl implements ManagerUserService {

    private final UserRepository userRepository;

    private final AssessmentProjectRepository projectRepository;

    private final AssessmentProjectQuestionRepository projectQuestionRepository;

    private final SkillRepository skillRepository;

    private final StaffEvaluationUserRepository staffEvaluationUserRepository;

    public List<ManagerUserDto> findAll(ManagerUserFilter filter) {
        var context = assessmentProjectContextOf(filter);

        var userFilter = UserFilter.builder()
            .search(filter.search())
            .projectRole(filter.projectRole())
            .role(USER)
            .build();

        //TODO: Fetching all users isn't the best approach in terms of performance,
        // we should think about a better way to handle this in the future.
        var employees = userRepository.findAll(userFilterSpecification(userFilter));

        var staffEvaluationUsers = filter.staffEvaluation() == null
            ? staffEvaluationUserRepository.findLastByStatusForAllUsers(COMPLETED)
            : staffEvaluationUserRepository.findByStaffEvaluationIdAndStatus(filter.staffEvaluation(), COMPLETED);

        var staffEvaluationUserMap = staffEvaluationUsers.stream()
            .collect(toMap(staffEvaluationUser -> staffEvaluationUser.getUser().getId(), identity()));

        return employees.stream()
            .map(em -> ofNullable(staffEvaluationUserMap.get(em.getId()))
                .map(seu -> dtoOf(em, calculateSkillLevelsForEmployee(em, seu, context)))
                .orElseGet(() -> dtoOf(em, defaultSkillLevelsForEmployee(context)))
            ).filter(user -> matchesCareerLevel(user, filter))
            .sorted(comparing(ManagerUserDto::lastName))
            .toList();
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
            .map(it -> ManagerSkillLevelDto.builder().skillId(it.getId()).skill(it.getName()).build()).toList();
    }

    private List<ManagerSkillLevelDto> calculateSkillLevelsForEmployee(User user,
                                                                       StaffEvaluationUser staffEvaluationUser,
                                                                       AssessmentProjectContext context
    ) {
        var yesQuestionIds = getYesQuestionIds(staffEvaluationUser);
        var projectQuestion = context.projectQuestionMap.getOrDefault(user.getProjectRole(), new HashMap<>());
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

    private boolean matchesCareerLevel(ManagerUserDto user, ManagerUserFilter filter) {
        if (filter.careerLevel() == null) {
            return true;
        }
        if (filter.skill() == null) {
            return false;
        }
        return user.skillLevels().stream()
            .filter(level -> filter.skill().equals(level.skillId()))
            .anyMatch(level -> filter.careerLevel().equals(level.careerLevelId()));
    }

    private ManagerUserDto dtoOf(User user, List<ManagerSkillLevelDto> skillLevels) {
        return ManagerUserDto.builder()
            .id(user.getId())
            .lastName(user.getLastName())
            .middleName(user.getMiddleName())
            .firstName(user.getFirstName())
            .username(user.getUsername())
            .email(user.getEmail())
            .projectRoleId(user.getProjectRole() == null ? null : user.getProjectRole().getId())
            .projectRole(user.getProjectRole() == null ? null : user.getProjectRole().getName())
            .skillLevels(skillLevels)
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
        return staffEvaluationUser.getAnswers().stream()
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
