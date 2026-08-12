package ru.otus.services.staffevaluation;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.otus.entity.enums.StaffEvaluationStatus.ACTIVE;

class StaffEvaluationServiceImplTest {

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

    private StaffEvaluation evaluation() {
        var projectRole = ProjectRole.builder().id(1L).build();
        var user = User.builder()
            .firstName("Иван")
            .lastName("Иванов")
            .middleName("")
            .projectRole(projectRole)
            .email("employee@example.com")
            .build();
        var evaluation = StaffEvaluation.builder()
            .id(1L)
            .name("Итоговая оценка")
            .dateFrom(LocalDate.of(2026, 8, 1))
            .dateTo(LocalDate.of(2026, 8, 31))
            .build();
        evaluation.setStaffEvaluationUsers(Set.of(
            StaffEvaluationUser.builder().user(user).staffEvaluation(evaluation).build()
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
