package ru.otus.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.otus.entity.StaffEvaluation;
import ru.otus.entity.StaffEvaluationAnswer;
import ru.otus.entity.StaffEvaluationUser;
import ru.otus.entity.User;
import ru.otus.repositories.StaffEvaluationAnswerRepository;
import ru.otus.repositories.StaffEvaluationUserRepository;
import ru.otus.services.verification.VerificationServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.otus.entity.enums.AnswerResponse.NO;
import static ru.otus.entity.enums.AnswerResponse.YES;
import static ru.otus.entity.enums.StaffEvaluationStatus.ACTIVE;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.VERIFICATION;

@DisplayName("Сервис проверки ответов")
@ExtendWith(MockitoExtension.class)
class VerificationServiceImplTest {

    @Mock
    private StaffEvaluationUserRepository staffEvaluationUserRepository;

    @Mock
    private StaffEvaluationAnswerRepository staffEvaluationAnswerRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private VerificationServiceImpl verificationService;

    @Test
    @DisplayName("список должен содержать статусы оценки и назначения")
    void shouldMapEvaluationAndAssignmentStatuses() {
        var staffEvaluation = StaffEvaluation.builder()
            .id(5L).name("Оценка 2026").status(ACTIVE)
            .build();
        var employee = User.builder()
            .id(10L).firstName("Иван").lastName("Иванов").username("ivanov")
            .build();
        var assignment = StaffEvaluationUser.builder()
            .id(12L).staffEvaluation(staffEvaluation).user(employee).status(VERIFICATION)
            .build();
        var pageable = Pageable.unpaged();
        when(staffEvaluationUserRepository.findByUserResponsibleIdAndStatus(9L, VERIFICATION, pageable))
            .thenReturn(new PageImpl<>(List.of(assignment)));
        var result = verificationService.findPending(9L, pageable);
        assertThat(result.getContent()).singleElement().satisfies(item -> {
            assertThat(item.staffEvaluationStatus()).isEqualTo(ACTIVE);
            assertThat(item.staffEvaluationUserStatus()).isEqualTo(VERIFICATION);
        });
    }

    @Test
    @DisplayName("подтверждение всех ответов не должно завершать проверку")
    void shouldConfirmAllAnswersWithoutCompletingVerification() {
        var verifier = User.builder().id(9L).build();
        var employee = User.builder().id(10L).responsible(verifier).build();
        var assignment = StaffEvaluationUser.builder()
            .id(12L)
            .user(employee)
            .status(VERIFICATION)
            .build();
        var firstAnswer = StaffEvaluationAnswer.builder().id(21L).verifiedResponse(NO).build();
        var secondAnswer = StaffEvaluationAnswer.builder().id(22L).build();
        var answers = List.of(firstAnswer, secondAnswer);

        when(staffEvaluationUserRepository.findByIdAndUserResponsibleIdAndStatus(12L, 9L, VERIFICATION))
            .thenReturn(Optional.of(assignment));
        when(staffEvaluationAnswerRepository.findByStaffEvaluationUserId(12L)).thenReturn(answers);

        verificationService.confirmAll(12L, 9L);

        assertThat(answers).allMatch(answer -> answer.getVerifiedResponse() == YES);
        assertThat(assignment.getStatus()).isEqualTo(VERIFICATION);
        assertThat(assignment.getVerifiedBy()).isNull();
        verify(staffEvaluationAnswerRepository).saveAll(answers);
    }
}
