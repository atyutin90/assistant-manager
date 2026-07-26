package ru.otus.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.dto.IdAndValue;
import ru.otus.dto.UserDto;
import ru.otus.dto.filter.UserFilter;
import ru.otus.entity.CareerLevel;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.User;
import ru.otus.entity.converters.UserDtoConverter;
import ru.otus.entity.enums.UserRole;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.repositories.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.dto.filter.specification.UserSpecification.userFilterSpecification;
import static ru.otus.entity.converters.UserDtoConverter.displayNameOf;
import static ru.otus.entity.converters.UserDtoConverter.dtoOf;
import static ru.otus.entity.enums.UserRole.TEAM_LEAD;
import static ru.otus.entity.enums.UserRole.USER;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Value("${app.default-password}")
    private String defaultPassword;

    private final UserRepository userRepository;

    private final MessageSource messageSource;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
            .map(UserDtoConverter::dtoOf);
    }

    @Override
    public Page<UserDto> findAll(UserFilter filter, Pageable pageable) {
        var userSpecification = userFilterSpecification(filter);
        return userRepository.findAll(userSpecification, pageable)
            .map(UserDtoConverter::dtoOf);
    }

    @Override
    public UserDto findById(Long id) {
        return userRepository.findById(id)
            .map(UserDtoConverter::dtoOf)
            .orElseThrow(() -> notFoundException(id));
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    @Override
    public UserDto create(UserDto data) {
        validate(data);
        return dtoOf(userRepository.save(of(null, data)));
    }

    @Transactional
    @Override
    public UserDto update(UserDto data) {
        var oUser = userRepository.findById(data.id());
        if (oUser.isEmpty()) {
            throw notFoundException(data.id());
        } else {
            validate(data);
            return dtoOf(userRepository.save(of(oUser.get(), data)));
        }
    }

    @Transactional
    @Override
    public void changePassword(Long id, String newPassword) {
        var user = userRepository.findById(id).orElseThrow(() -> notFoundException(id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public List<IdAndValue> findTeamLeads(Long excludedUserId) {
        var teamLeads = excludedUserId != null
            ? userRepository.findByRolesContainsAndIdNot(TEAM_LEAD, excludedUserId)
            : userRepository.findByRolesContains(TEAM_LEAD);
        return teamLeads.stream()
            .map(user -> IdAndValue.builder()
                .id(user.getId())
                .value(displayNameOf(user))
                .build())
            .toList();
    }

    private User of(User oldUser, UserDto data) {
        return User.builder()
            .id(data.id())
            .lastName(data.lastName())
            .middleName(data.middleName())
            .firstName(data.firstName())
            .username(data.username())
            .email(data.email())
            .projectRole(
                ofNullable(data.projectRole()).map(it -> ProjectRole.builder().id(it).build()).orElse(null)
            )
            .currentLevel(
                ofNullable(data.currentLevel()).map(it -> CareerLevel.builder().id(it).build()).orElse(null)
            )
            .laborCodePosition(data.laborCodePosition())
            .responsible(resolveResponsible(data))
            .roles(data.userRoles().stream()
                .map(UserRole::userRoleOf)
                .map(it -> it.orElse(null))
                .filter(Objects::nonNull)
                .collect(toSet()))
            .password(resolvePassword(oldUser, data))
            .build();
    }

    private User resolveResponsible(UserDto data) {
        if (!data.userRoles().contains(USER.name()) || data.responsibleId() == null) {
            return null;
        }

        return userRepository.findById(data.responsibleId())
            .filter(user -> user.getRoles().contains(TEAM_LEAD))
            .filter(user -> !user.getId().equals(data.id()))
            .orElseThrow(() -> notFoundException(data.responsibleId()));
    }

    private String resolvePassword(User oldUser, UserDto data) {
        var result = oldUser != null ? oldUser.getPassword() : passwordEncoder.encode(defaultPassword);
        if (isNotBlank(data.password())) {
            result =  passwordEncoder.encode(data.password());
        }
        return result;
    }

    private DataNotFoundException notFoundException(Long id) {
        return new DataNotFoundException(
            messageSource.getMessage("error.not-found-data-with-id", new Object[]{id}, getLocale()));
    }

    private void validate(UserDto data) {
        Map<String, String> map = new HashMap<>();
        if (userRepository.existsByUsernameAndIdNot(data.username(),  data.id())) {
            map.put("username", messageSource.getMessage("error.non-unique-value", null, getLocale()));
        }
        if (!map.isEmpty()) {
            throw new NonUniqueValueException(map);
        }
    }
}
