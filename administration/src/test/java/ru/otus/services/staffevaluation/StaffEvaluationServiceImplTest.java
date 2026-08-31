package ru.otus.services.staffevaluation;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.Specification;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.dto.filter.UserFilter;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.Question;
import ru.otus.entity.StaffEvaluation;
import ru.otus.entity.StaffEvaluationQuestion;
import ru.otus.entity.StaffEvaluationUser;
import ru.otus.entity.User;
import ru.otus.messaging.EmailMessage;
import ru.otus.repositories.QuestionRepository;
import ru.otus.repositories.StaffEvaluationRepository;
import ru.otus.repositories.UserRepository;
import ru.otus.services.EmailMessageService;
import ru.otus.services.email.StaffEvaluationActivationEmailFactory;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.otus.entity.enums.StaffEvaluationStatus.ACTIVE;

class StaffEvaluationServiceImplTest {

    @Test
    void shouldRemoveAllStaffEvaluationEmployeesMatchingFilter() {
        var staffEvaluationRepository = mock(StaffEvaluationRepository.class);
        var userRepository = mock(UserRepository.class);
        var retainedUser = User.builder().id(10L).projectRoles(Set.of(ProjectRole.builder().id(2L).build())).build();
        var removedUser = User.builder().id(20L).projectRoles(Set.of(ProjectRole.builder().id(1L).build())).build();
        var staffEvaluation = StaffEvaluation.builder().id(1L).build();
        staffEvaluation.setStaffEvaluationUsers(new HashSet<>(Set.of(
            StaffEvaluationUser.builder().user(retainedUser).projectRole(retainedUser.getProjectRoles().iterator().next())
                .staffEvaluation(staffEvaluation).build(),
            StaffEvaluationUser.builder().user(removedUser).projectRole(removedUser.getProjectRoles().iterator().next())
                .staffEvaluation(staffEvaluation).build()
        )));
        when(staffEvaluationRepository.findById(1L)).thenReturn(Optional.of(staffEvaluation));
        when(userRepository.findAll(org.mockito.ArgumentMatchers.<Specification<User>>any()))
            .thenReturn(List.of(removedUser));
        var service = new StaffEvaluationServiceImpl(
            staffEvaluationRepository,
            userRepository,
            mock(QuestionRepository.class),
            mock(MessageSource.class),
            mock(EmailMessageService.class),
            mock(StaffEvaluationActivationEmailFactory.class)
        );

        service.removeEmployees(1L, UserFilter.builder().projectRole(1L).build());

        assertEquals(Set.of(10L), staffEvaluation.getStaffEvaluationUsers().stream()
            .map(StaffEvaluationUser::getUser)
            .map(User::getId)
            .collect(java.util.stream.Collectors.toSet()));
        verify(staffEvaluationRepository).save(staffEvaluation);
    }

    @Test
    void shouldRemoveAllStaffEvaluationQuestionMatchingFilter() {
        var staffEvaluationRepository = mock(StaffEvaluationRepository.class);
        var questionRepository = mock(QuestionRepository.class);
        var retainedQuestion = Question.builder().id(10L).projectRole(ProjectRole.builder().id(2L).build()).build();
        var removedQuestion = Question.builder().id(20L).projectRole(ProjectRole.builder().id(1L).build()).build();
        var staffEvaluation = StaffEvaluation.builder()
            .id(1L)
            .staffEvaluationQuestions(new HashSet<>(Set.of(
                StaffEvaluationQuestion.builder().question(retainedQuestion).build(),
                StaffEvaluationQuestion.builder().question(removedQuestion).build()
            ))).build();
        when(staffEvaluationRepository.findById(1L)).thenReturn(Optional.of(staffEvaluation));
        when(questionRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Question>>any()))
            .thenReturn(List.of(removedQuestion));
        var service = new StaffEvaluationServiceImpl(
            staffEvaluationRepository,
            mock(UserRepository.class),
            questionRepository,
            mock(MessageSource.class),
            mock(EmailMessageService.class),
            mock(StaffEvaluationActivationEmailFactory.class)
        );

        service.removeQuestions(1L, QuestionFilter.builder().projectRole(1L).build());

        assertEquals(Set.of(10L), staffEvaluation.getStaffEvaluationQuestions().stream()
            .map(StaffEvaluationQuestion::getQuestion)
            .map(Question::getId)
            .collect(java.util.stream.Collectors.toSet()));
        verify(staffEvaluationRepository).save(staffEvaluation);
    }

    @Test
    void shouldPublishEmailWhenEvaluationStarts() {
        var evaluationRepository = mock(StaffEvaluationRepository.class);
        var emailMessageService = mock(EmailMessageService.class);
        var evaluation = evaluation();
        var emailMessage = new EmailMessage("employee@example.com", "Subject", "<p>Text</p>");
        var activationEmailFactory = mock(StaffEvaluationActivationEmailFactory.class);
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation));
        when(activationEmailFactory.create(evaluation, evaluation.getStaffEvaluationUsers().iterator().next().getUser()))
            .thenReturn(emailMessage);
        var service = new StaffEvaluationServiceImpl(
            evaluationRepository,
            mock(UserRepository.class),
            mock(QuestionRepository.class),
            mock(MessageSource.class),
            emailMessageService,
            activationEmailFactory
        );

        service.start(1L);

        verify(evaluationRepository).save(evaluation);
        verify(emailMessageService).send(emailMessage);
        assertEquals(ACTIVE, evaluation.getStatus());
    }

    @Test
    void shouldCreateAssignmentForEveryEmployeeProjectRole() {
        var evaluationRepository = mock(StaffEvaluationRepository.class);
        var userRepository = mock(UserRepository.class);
        var secondRole = ProjectRole.builder().id(2L).build();
        var firstRole = ProjectRole.builder().id(1L).build();
        var employee = User.builder().id(7L).projectRoles(Set.of(firstRole, secondRole)).build();
        var evaluation = StaffEvaluation.builder().id(1L).build();
        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(evaluation));
        when(userRepository.findAllById(Set.of(7L))).thenReturn(List.of(employee));
        var service = new StaffEvaluationServiceImpl(
            evaluationRepository,
            userRepository,
            mock(QuestionRepository.class),
            mock(MessageSource.class),
            mock(EmailMessageService.class),
            mock(StaffEvaluationActivationEmailFactory.class)
        );

        service.addEmployees(1L, Set.of(7L));

        assertEquals(Set.of(1L, 2L), evaluation.getStaffEvaluationUsers().stream()
            .map(StaffEvaluationUser::getProjectRole)
            .map(ProjectRole::getId)
            .collect(toSet()));
    }

    private StaffEvaluation evaluation() {
        var projectRole = ProjectRole.builder().id(1L).build();
        var user = User.builder()
            .firstName("Иван")
            .lastName("Иванов")
            .middleName("")
            .projectRoles(new HashSet<>(Set.of(projectRole)))
            .email("employee@example.com")
            .build();
        var evaluation = StaffEvaluation.builder()
            .id(1L)
            .name("Итоговая оценка")
            .dateFrom(LocalDate.of(2026, 8, 1))
            .dateTo(LocalDate.of(2026, 8, 31))
            .build();
        evaluation.setStaffEvaluationUsers(Set.of(
            StaffEvaluationUser.builder().user(user).projectRole(projectRole).staffEvaluation(evaluation).build()
        ));
        evaluation.setStaffEvaluationQuestions(Set.of(
            StaffEvaluationQuestion.builder()
                .question(Question.builder()
                    .id(10L)
                    .projectRole(projectRole)
                    .build())
                .staffEvaluation(evaluation)
                .build()
        ));
        return evaluation;
    }
}
