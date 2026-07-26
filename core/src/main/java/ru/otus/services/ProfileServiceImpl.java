package ru.otus.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.dto.ProfileDto;
import ru.otus.entity.User;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.repositories.UserRepository;

import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    private final UserService userService;

    private final MessageSource messageSource;

    @Override
    public ProfileDto getProfile(Long userId) {
        return userRepository.findById(userId).map(this::profileDtoOf).orElseThrow(this::notUserFoundException);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        var user = userRepository.findById(userId).orElseThrow(this::notUserFoundException);
        userService.changePassword(user.getId(), newPassword);
    }

    private ProfileDto profileDtoOf(User user) {
        return ProfileDto.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .middleName(user.getMiddleName())
            .username(user.getUsername())
            .projectRole(user.getProjectRole() != null ? user.getProjectRole().getName() : null)
            .currentLevel(user.getCurrentLevel() != null ? user.getCurrentLevel().getName() : null)
            .laborCodePosition(user.getLaborCodePosition())
            .build();
    }

    private DataNotFoundException notUserFoundException() {
        return new DataNotFoundException(
            messageSource.getMessage("error.invalid-username", new Object[]{}, getLocale()));
    }
}
