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
import ru.otus.entity.ProjectRole;
import ru.otus.entity.User;
import ru.otus.exceptions.WebApplicationException;
import ru.otus.repositories.StaffEvaluationAnswerRepository;
import ru.otus.repositories.StaffEvaluationUserRepository;
import ru.otus.services.verification.VerificationServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.otus.entity.enums.AnswerResponse.NO;
import static ru.otus.entity.enums.AnswerResponse.YES;
import static ru.otus.entity.enums.StaffEvaluationStatus.ACTIVE;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.VERIFICATION;
import static ru.otus.entity.enums.StaffEvaluationUserStatus.COMPLETED;

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
            .id(5L)
            .name("Оценка 2026")
            .status(ACTIVE)
            .build();
        var employee = User.builder()
            .id(10L)
            .firstName("Иван")
            .lastName("Иванов")
            .username("ivanov")
            .build();
        var assignment = StaffEvaluationUser.builder()
            .id(12L).staffEvaluation(staffEvaluation).user(employee).status(VERIFICATION)
            .build();
        var pageable = Pageable.unpaged();
        when(staffEvaluationUserRepository.findByUserResponsiblesIdAndStatus(9L, VERIFICATION, pageable))
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
        var verifier = User.builder()
            .id(9L)
            .build();
        var employee = User.builder()
            .id(10L)
            .responsibles(Set.of(verifier))
            .build();
        var assignment = StaffEvaluationUser.builder()
            .id(12L)
            .user(employee)
            .verificationOwner(verifier)
            .status(VERIFICATION)
            .build();
        var firstAnswer = StaffEvaluationAnswer.builder()
            .id(21L)
            .verifiedResponse(NO)
            .build();
        var secondAnswer = StaffEvaluationAnswer.builder()
            .id(22L)
            .build();
        var answers = List.of(firstAnswer, secondAnswer);

        when(staffEvaluationUserRepository.findForUpdateByIdAndUserResponsiblesIdAndStatus(12L, 9L, VERIFICATION))
            .thenReturn(Optional.of(assignment));
        when(staffEvaluationAnswerRepository.findByStaffEvaluationUserId(12L)).thenReturn(answers);

        verificationService.confirmAll(12L, 9L);

        assertThat(answers).allMatch(answer -> answer.getVerifiedResponse() == YES);
        assertThat(assignment.getStatus()).isEqualTo(VERIFICATION);
        assertThat(assignment.getVerifiedBy()).isNull();
        verify(staffEvaluationAnswerRepository).saveAll(answers);
    }

    @Test
    @DisplayName("завершивший проверку ответственный должен соответствовать проектной роли")
    void shouldCompleteVerificationByResponsibleForProjectRole() {
        var qa = ProjectRole.builder()
            .id(3L)
            .build();
        var verifier = User.builder()
            .id(9L)
            .projectRoles(Set.of(qa))
            .build();
        var employee = User.builder()
            .id(10L)
            .responsibles(Set.of(verifier))
            .build();
        var assignment = StaffEvaluationUser.builder()
            .id(12L)
            .user(employee)
            .projectRole(qa)
            .verificationOwner(verifier)
            .status(VERIFICATION)
            .build();
        var answers = List.of(StaffEvaluationAnswer.builder().id(21L).verifiedResponse(YES).build());
        when(staffEvaluationUserRepository.findForUpdateByIdAndUserResponsiblesIdAndStatus(12L, 9L, VERIFICATION))
            .thenReturn(Optional.of(assignment));
        when(staffEvaluationAnswerRepository.findByStaffEvaluationUserId(12L)).thenReturn(answers);

        verificationService.complete(12L, 9L);

        assertThat(assignment.getStatus()).isEqualTo(COMPLETED);
        assertThat(assignment.getVerifiedBy()).isSameAs(verifier);
        assertThat(assignment.getVerificationOwner()).isNull();
        verify(staffEvaluationUserRepository).save(assignment);
    }

    @Test
    @DisplayName("другой подходящий ответственный должен перехватывать проверку")
    void shouldTransferVerificationToAnotherResponsible() {
        var qa = ProjectRole.builder()
            .id(3L)
            .build();
        var previousOwner = User.builder()
            .id(8L)
            .projectRoles(Set.of(qa))
            .build();
        var newOwner = User.builder()
            .id(9L)
            .projectRoles(Set.of(qa))
            .build();
        var employee = User.builder()
            .id(10L)
            .responsibles(Set.of(previousOwner, newOwner))
            .build();
        var assignment = StaffEvaluationUser.builder()
            .id(12L)
            .user(employee)
            .projectRole(qa)
            .verificationOwner(previousOwner)
            .status(VERIFICATION)
            .build();
        when(staffEvaluationUserRepository.findForUpdateByIdAndUserResponsiblesIdAndStatus(12L, 9L, VERIFICATION))
            .thenReturn(Optional.of(assignment));

        verificationService.take(12L, 9L);

        assertThat(assignment.getVerificationOwner()).isSameAs(newOwner);
        verify(staffEvaluationUserRepository).save(assignment);
    }

    @Test
    @DisplayName("ответственный в режиме просмотра не должен изменять проверку")
    void shouldRejectChangesFromNonOwner() {
        var assignment = StaffEvaluationUser.builder()
            .id(12L)
            .verificationOwner(User.builder().id(8L).build())
            .status(VERIFICATION)
            .build();
        when(staffEvaluationUserRepository.findForUpdateByIdAndUserResponsiblesIdAndStatus(12L, 9L, VERIFICATION))
            .thenReturn(Optional.of(assignment));
        when(messageSource.getMessage(eq("error.verification-not-owned"), any(), any()))
            .thenReturn("not owned");

        assertThatThrownBy(() -> verificationService.confirmAll(12L, 9L))
            .isInstanceOf(WebApplicationException.class)
            .hasMessage("not owned");
        verify(staffEvaluationAnswerRepository, never()).findByStaffEvaluationUserId(anyLong());
    }
}
