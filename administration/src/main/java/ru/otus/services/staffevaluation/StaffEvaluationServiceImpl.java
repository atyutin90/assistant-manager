package ru.otus.services.staffevaluation;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.dto.StaffEvaluationEmployeeDto;
import ru.otus.dto.StaffEvaluationQuestionsDto;
import ru.otus.dto.StaffEvaluationDto;
import ru.otus.dto.filter.StaffEvaluationFilter;
import ru.otus.dto.filter.UserFilter;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.Question;
import ru.otus.entity.StaffEvaluation;
import ru.otus.entity.StaffEvaluationQuestion;
import ru.otus.entity.User;
import ru.otus.entity.StaffEvaluationUser;
import ru.otus.entity.enums.StaffEvaluationStatus;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.exceptions.StaffEvaluationStatusException;
import ru.otus.repositories.QuestionRepository;
import ru.otus.repositories.StaffEvaluationRepository;
import ru.otus.repositories.UserRepository;
import ru.otus.services.EmailMessageService;
import ru.otus.services.email.StaffEvaluationActivationEmailFactory;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.dto.filter.specification.QuestionSpecification.questionFilterSpecification;
import static ru.otus.dto.filter.specification.StaffEvaluationSpecification.staffEvaluationFilterSpecification;
import static ru.otus.dto.filter.specification.UserSpecification.userFilterSpecification;
import static ru.otus.entity.enums.StaffEvaluationStatus.ACTIVE;
import static ru.otus.entity.enums.StaffEvaluationStatus.COMPLETED;
import static ru.otus.entity.enums.StaffEvaluationStatus.DRAFT;

@Service
@RequiredArgsConstructor
public class StaffEvaluationServiceImpl implements StaffEvaluationService {

    private final StaffEvaluationRepository staffEvaluationRepository;

    private final UserRepository userRepository;

    private final QuestionRepository questionRepository;

    private final MessageSource messageSource;

    private final EmailMessageService emailMessageService;

    private final StaffEvaluationActivationEmailFactory activationEmailFactory;

    @Override
    public Page<StaffEvaluationDto> findAll(StaffEvaluationFilter filter, Pageable pageable) {
        var staffEvaluationSpecification = staffEvaluationFilterSpecification(filter);
        return staffEvaluationRepository.findAll(staffEvaluationSpecification, pageable)
            .map(this::dtoOf);
    }

    @Override
    public StaffEvaluationDto findById(Long id) {
        return staffEvaluationRepository.findById(id)
            .map(this::dtoOf)
            .orElseThrow(() -> notFoundException(id));
    }

    @Override
    public StaffEvaluationEmployeeDto findEmployeeById(Long id) {
        return staffEvaluationRepository.findById(id)
            .map(this::dtoEmployeeOf)
            .orElseThrow(() -> notFoundException(id));
    }

    @Override
    public StaffEvaluationQuestionsDto findQuestionsById(Long id) {
        return staffEvaluationRepository.findById(id)
            .map(this::dtoQuestionsOf)
            .orElseThrow(() -> notFoundException(id));
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        var staffEvaluation = findEntityById(id);
        checkDraftStatus(staffEvaluation, "error.staff-evaluation.delete-only-draft");
        staffEvaluationRepository.delete(staffEvaluation);
    }

    @Transactional
    @Override
    public StaffEvaluationDto create(StaffEvaluationDto data) {
        return dtoOf(staffEvaluationRepository.save(of(data)));
    }

    @Transactional
    @Override
    public StaffEvaluationDto update(StaffEvaluationDto data) {
        return staffEvaluationRepository.findById(data.id())
            .map(staffEvaluation -> {
                checkDraftStatus(staffEvaluation, "error.staff-evaluation.update-only-draft");
                return dtoOf(staffEvaluationRepository.save(of(staffEvaluation, data)));
            })
            .orElseThrow(() -> notFoundException(data.id()));
    }

    @Transactional
    @Override
    public void start(Long id) {
        var staffEvaluation = findEntityById(id);
        checkStatus(staffEvaluation, DRAFT, "error.staff-evaluation.start-only-draft");
        checkBeforeStart(staffEvaluation);
        staffEvaluation.setStatus(ACTIVE);
        staffEvaluationRepository.save(staffEvaluation);
        sendActivationMessages(staffEvaluation);
    }

    @Transactional
    @Override
    public void complete(Long id) {
        var staffEvaluation = findEntityById(id);
        checkStatus(staffEvaluation, ACTIVE, "error.staff-evaluation.complete-only-active");
        staffEvaluation.setStatus(COMPLETED);
        dtoOf(staffEvaluationRepository.save(staffEvaluation));
    }

    @Transactional
    @Override
    public void addEmployees(Long staffEvaluationId, Set<Long> employeeIds) {
        var staffEvaluation = findEntityById(staffEvaluationId);
        checkCompletedStatus(staffEvaluation);
        addEmployeeAssignments(staffEvaluation, employeeIds);
        staffEvaluationRepository.save(staffEvaluation);
    }

    @Transactional
    @Override
    public void addEmployees(Long staffEvaluationId, UserFilter filter) {
        var staffEvaluation = findEntityById(staffEvaluationId);
        checkCompletedStatus(staffEvaluation);
        var users = userRepository.findAll(userFilterSpecification(filter));
        addEmployeeAssignments(staffEvaluation, users.stream().map(User::getId).collect(toSet()));
        staffEvaluationRepository.save(staffEvaluation);
    }

    @Transactional
    @Override
    public void removeEmployees(Long staffEvaluationId, Set<Long> employeeIds) {
        var staffEvaluation = findEntityById(staffEvaluationId);
        checkDraftStatus(staffEvaluation, "error.staff-evaluation.remove-only-draft");
        staffEvaluation.getStaffEvaluationUsers()
            .removeIf(it -> it.getUser() != null && employeeIds.contains(it.getUser().getId()));
        staffEvaluationRepository.save(staffEvaluation);
    }

    @Transactional
    @Override
    public void removeEmployees(Long staffEvaluationId, UserFilter filter) {
        var employeeIds = userRepository.findAll(userFilterSpecification(filter)).stream()
            .map(User::getId)
            .collect(toSet());
        removeEmployees(staffEvaluationId, employeeIds);
    }

    @Transactional
    @Override
    public void addQuestions(Long staffEvaluationId, Set<Long> questionIds) {
        var staffEvaluation = findEntityById(staffEvaluationId);
        checkCompletedStatus(staffEvaluation);
        var questions = questionRepository.findAllById(questionIds);
        addQuestionAssignments(staffEvaluation, questions);
        syncAllAnswers(staffEvaluation);
        staffEvaluationRepository.save(staffEvaluation);
    }

    @Transactional
    @Override
    public void addQuestions(Long staffEvaluationId, QuestionFilter filter) {
        var staffEvaluation = findEntityById(staffEvaluationId);
        checkCompletedStatus(staffEvaluation);
        var questions = questionRepository.findAll(questionFilterSpecification(filter));
        addQuestionAssignments(staffEvaluation, questions);
        syncAllAnswers(staffEvaluation);
        staffEvaluationRepository.save(staffEvaluation);
    }

    @Transactional
    @Override
    public void removeQuestions(Long staffEvaluationId, Set<Long> questionIds) {
        var staffEvaluation = findEntityById(staffEvaluationId);
        checkDraftStatus(staffEvaluation, "error.staff-evaluation.remove-only-draft");
        staffEvaluation.getStaffEvaluationQuestions().removeIf(it ->
            it.getQuestion() != null && questionIds.contains(it.getQuestion().getId()));
        syncAllAnswers(staffEvaluation);
        staffEvaluationRepository.save(staffEvaluation);
    }

    @Transactional
    @Override
    public void removeQuestions(Long staffEvaluationId, QuestionFilter filter) {
        var staffEvaluation = findEntityById(staffEvaluationId);
        checkDraftStatus(staffEvaluation, "error.staff-evaluation.remove-only-draft");
        var questionIds = questionRepository.findAll(questionFilterSpecification(filter)).stream()
            .map(Question::getId)
            .toList();
        staffEvaluation.getStaffEvaluationQuestions().removeIf(it ->
            it.getQuestion() != null && questionIds.contains(it.getQuestion().getId()));
        syncAllAnswers(staffEvaluation);
        staffEvaluationRepository.save(staffEvaluation);
    }

    @Transactional
    @Override
    public void updateQuestionPositions(Long staffEvaluationId, Map<Long, Integer> questionPositions) {
        var staffEvaluation = findEntityById(staffEvaluationId);
        checkCompletedStatus(staffEvaluation);
        staffEvaluation.getStaffEvaluationQuestions().forEach(seq -> {
            if (seq.getQuestion() != null) {
                var position = questionPositions.get(seq.getQuestion().getId());
                if (position != null) {
                    seq.setPosition(position);
                }
            }
        });
        staffEvaluationRepository.save(staffEvaluation);
    }

    private StaffEvaluationDto dtoOf(StaffEvaluation data) {
        return StaffEvaluationDto.builder()
            .id(data.getId())
            .status(data.getStatus())
            .dateFrom(data.getDateFrom())
            .dateTo(data.getDateTo())
            .name(data.getName())
            .build();
    }

    private StaffEvaluation of(StaffEvaluation oldStaffEvaluation, StaffEvaluationDto dto) {
        return StaffEvaluation.builder()
            .id(dto.id())
            .name(dto.name())
            .dateFrom(dto.dateFrom())
            .dateTo(dto.dateTo())
            .status(oldStaffEvaluation.getStatus())
            .staffEvaluationUsers(oldStaffEvaluation.getStaffEvaluationUsers())
            .staffEvaluationQuestions(oldStaffEvaluation.getStaffEvaluationQuestions())
            .build();
    }

    private StaffEvaluationEmployeeDto dtoEmployeeOf(StaffEvaluation data) {
        return StaffEvaluationEmployeeDto.builder()
            .id(data.getId())
            .name(data.getName())
            .status(data.getStatus())
            .employeeIds(data.getStaffEvaluationUsers().stream()
                .map(StaffEvaluationUser::getUser)
                .map(User::getId)
                .collect(toSet()))
            .build();
    }

    private StaffEvaluationQuestionsDto dtoQuestionsOf(StaffEvaluation data) {
        return StaffEvaluationQuestionsDto.builder()
            .id(data.getId())
            .name(data.getName())
            .status(data.getStatus())
            .questionIds(data.getStaffEvaluationQuestions().stream()
                .map(StaffEvaluationQuestion::getQuestion)
                .filter(Objects::nonNull)
                .map(Question::getId)
                .collect(toSet()))
            .build();
    }

    private StaffEvaluation of(StaffEvaluationDto dto) {
        return StaffEvaluation.builder()
            .id(dto.id())
            .name(dto.name())
            .dateFrom(dto.dateFrom())
            .dateTo(dto.dateTo())
            .build();
    }

    private StaffEvaluation findEntityById(Long id) {
        return staffEvaluationRepository.findById(id).orElseThrow(() -> notFoundException(id));
    }

    private void checkBeforeStart(StaffEvaluation staffEvaluation) {
        if (isEmpty(staffEvaluation.getStaffEvaluationUsers())) {
            throw statusExceptionOf("error.staff-evaluation.start-without-employees");
        }
        if (isEmpty(staffEvaluation.getStaffEvaluationQuestions())) {
            throw statusExceptionOf("error.staff-evaluation.start-without-questions");
        }

        if (checkCountQuestionsForProjectRole(staffEvaluation)) {
            throw statusExceptionOf("error.staff-evaluation.start-without-questions-for-project-role");
        }
    }

    private static boolean checkCountQuestionsForProjectRole(StaffEvaluation staffEvaluation) {
        Map<ProjectRole, Long> map = staffEvaluation.getStaffEvaluationQuestions().stream()
            .collect(Collectors.groupingBy(
                q -> q.getQuestion().getProjectRole(),
                Collectors.counting()
            ));

        return staffEvaluation.getStaffEvaluationUsers().stream()
            .map(StaffEvaluationUser::getUser)
            .filter(Objects::nonNull)
            .map(User::getProjectRole)
            .anyMatch(pr -> map.get(pr) == null || map.get(pr) == 0);
    }

    private void sendActivationMessages(StaffEvaluation staffEvaluation) {
        staffEvaluation.getStaffEvaluationUsers().stream()
            .map(StaffEvaluationUser::getUser)
            .filter(Objects::nonNull)
            .filter(user -> user.getEmail() != null && !user.getEmail().isBlank())
            .map(user -> activationEmailFactory.create(staffEvaluation, user))
            .forEach(emailMessageService::send);
    }

    private void checkCompletedStatus(StaffEvaluation staffEvaluation) {
        if (COMPLETED.equals(staffEvaluation.getStatus())) {
            throw statusExceptionOf("error.staff-evaluation.completed-read-only");
        }
    }

    private void checkDraftStatus(StaffEvaluation staffEvaluation, String messageCode) {
        checkStatus(staffEvaluation, DRAFT, messageCode);
    }

    private void checkStatus(StaffEvaluation staffEvaluation, StaffEvaluationStatus expected, String messageCode) {
        if (!staffEvaluation.getStatus().equals(expected)) {
            throw statusExceptionOf(messageCode);
        }
    }

    private StaffEvaluationStatusException statusExceptionOf(String messageCode) {
        return new StaffEvaluationStatusException(messageSource.getMessage(messageCode, null, getLocale()));
    }

    private void addQuestionAssignments(StaffEvaluation staffEvaluation, List<Question> questions) {
        var assignedQuestionIds = staffEvaluation.getStaffEvaluationQuestions().stream()
            .map(StaffEvaluationQuestion::getQuestion)
            .filter(Objects::nonNull)
            .map(Question::getId)
            .collect(toSet());

        for (Question question : questions) {
            if (!assignedQuestionIds.contains(question.getId())) {
                var nextPosition = staffEvaluation.getStaffEvaluationQuestions().stream()
                    .filter(seq -> seq.getQuestion().getProjectRole().equals(question.getProjectRole()))
                    .map(StaffEvaluationQuestion::getPosition)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder())
                    .orElse(0) + 1;
                staffEvaluation.getStaffEvaluationQuestions()
                    .add(StaffEvaluationQuestion.builder()
                        .staffEvaluation(staffEvaluation)
                        .question(question)
                        .position(nextPosition)
                        .build());
            }
        }
    }

    private void addEmployeeAssignments(StaffEvaluation staffEvaluation, Set<Long> employeeIds) {
        var assignedUserIds = staffEvaluation.getStaffEvaluationUsers().stream()
            .map(StaffEvaluationUser::getUser)
            .filter(Objects::nonNull)
            .map(User::getId)
            .collect(toSet());

        for (Long id : employeeIds) {
            if (assignedUserIds.contains(id)) {
                continue;
            }
            var assignment = StaffEvaluationUser.builder()
                .staffEvaluation(staffEvaluation)
                .user(User.builder().id(id).build())
                .answers(new HashSet<>())
                .build();
            staffEvaluation.getStaffEvaluationUsers().add(assignment);
        }
    }

    private void syncAllAnswers(StaffEvaluation staffEvaluation) {
        var questionMap = staffEvaluation.getStaffEvaluationQuestions().stream()
            .map(StaffEvaluationQuestion::getQuestion)
            .filter(Objects::nonNull)
            .collect(toMap(Question::getId, identity()));
        staffEvaluation.getStaffEvaluationUsers().forEach(it -> syncAnswers(it, questionMap));
    }

    private void syncAnswers(StaffEvaluationUser staffEvaluationUser, Map<Long, Question> questionMap) {
        staffEvaluationUser.getAnswers()
            .removeIf(it -> it.getQuestion() == null || !questionMap.containsKey(it.getQuestion().getId()));
    }

    private DataNotFoundException notFoundException(Long id) {
        return new DataNotFoundException(
            messageSource.getMessage("error.not-found-data-with-id", new Object[]{id}, getLocale())
        );
    }
}
