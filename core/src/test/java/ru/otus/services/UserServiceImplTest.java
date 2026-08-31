package ru.otus.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.otus.dto.UserDto;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.User;
import ru.otus.entity.enums.UserRole;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.repositories.UserRepository;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Сервис пользователей")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("должен сохранять нескольких ответственных, покрывающих роли сотрудника")
    void shouldSaveResponsiblesCoveringEmployeeRoles() {
        var qa = ProjectRole.builder()
            .id(1L)
            .build();
        var backend = ProjectRole.builder()
            .id(2L)
            .build();
        var qaLead = teamLead(10L, qa);
        var backendLead = teamLead(11L, backend);
        when(userRepository.findAllById(Set.of(10L, 11L))).thenReturn(List.of(qaLead, backendLead));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.create(employee(Set.of(1L, 2L), Set.of(10L, 11L)));

        var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getResponsibles()).containsExactlyInAnyOrder(qaLead, backendLead);
    }

    @Test
    @DisplayName("не должен сохранять сотрудника с непокрытой проектной ролью")
    void shouldRejectResponsiblesNotCoveringEmployeeRoles() {
        var qaLead = teamLead(10L, ProjectRole.builder().id(1L).build());
        when(userRepository.findAllById(Set.of(10L))).thenReturn(List.of(qaLead));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("validation error");

        assertThatThrownBy(() -> userService.create(employee(Set.of(1L, 2L), Set.of(10L))))
            .isInstanceOfSatisfying(NonUniqueValueException.class, exception ->
                assertThat(exception.getInfo()).containsKey("responsibleIds"));
    }

    private User teamLead(Long id, ProjectRole... projectRoles) {
        return User.builder()
            .id(id)
            .roles(Set.of(UserRole.TEAM_LEAD))
            .projectRoles(Set.of(projectRoles))
            .build();
    }

    private UserDto employee(Set<Long> projectRoles, Set<Long> responsibleIds) {
        return UserDto.builder()
            .lastName("Иванов")
            .middleName("Иванович")
            .firstName("Иван")
            .username("ivan")
            .email("ivan@example.com")
            .projectRoles(projectRoles)
            .responsibleIds(responsibleIds)
            .userRoles(Set.of(UserRole.USER.name()))
            .build();
    }
}
