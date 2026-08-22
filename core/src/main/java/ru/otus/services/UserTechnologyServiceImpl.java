package ru.otus.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.converters.UserTechnologyConverter;
import ru.otus.dto.UserTechnologyDto;
import ru.otus.dto.filter.UserTechnologyFilter;
import ru.otus.entity.Technology;
import ru.otus.entity.User;
import ru.otus.entity.UserTechnology;
import ru.otus.entity.enums.TechnologyLevel;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.repositories.TechnologyRepository;
import ru.otus.repositories.UserRepository;
import ru.otus.repositories.UserTechnologyRepository;

import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.dto.filter.specification.UserTechnologySpecification.technologyFilterSpecification;
import static ru.otus.entity.enums.TechnologyLevel.BASIC_KNOWLEDGE;

@Service
@RequiredArgsConstructor
public class UserTechnologyServiceImpl implements UserTechnologyService {

    private final UserTechnologyRepository userTechnologyRepository;

    private final UserRepository userRepository;

    private final TechnologyRepository technologyRepository;

    private final MessageSource messageSource;

    @Override
    public Page<UserTechnologyDto> findAll(UserTechnologyFilter filter, Pageable pageable) {
        return userTechnologyRepository.findAll(technologyFilterSpecification(filter), pageable)
            .map(UserTechnologyConverter::dtoOf);
    }

    @Override
    @Transactional
    public void addSelected(Long userId, Set<Long> technologyIds) {
        if (isNotEmpty(technologyIds)) {
            var user = userRepository.findById(userId).orElseThrow(() -> userNotFoundException(userId));
            technologyRepository.findByIdInAndEnabledTrue(technologyIds)
                .forEach(technology -> saveIfNotExists(user, technology));
        }
    }

    @Override
    @Transactional
    public void changeLevel(Long userId, Long technologyId, TechnologyLevel level) {
        var userTechnology = findUserTechnology(userId, technologyId);
        userTechnology.setLevel(level);
        userTechnologyRepository.save(userTechnology);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long technologyId) {
        userTechnologyRepository.delete(findUserTechnology(userId, technologyId));
    }

    private UserTechnology findUserTechnology(Long userId, Long technologyId) {
        return userTechnologyRepository.findByTechnologyIdAndUserId(technologyId, userId)
            .orElseThrow(this::technologyNotFoundException);
    }

    private void saveIfNotExists(User user, Technology technology) {
        if (!userTechnologyRepository.existsByUserIdAndTechnologyId(user.getId(), technology.getId())) {
            userTechnologyRepository.save(UserTechnology.builder()
                .user(user)
                .technology(technology)
                .level(BASIC_KNOWLEDGE)
                .build());
        }
    }

    private DataNotFoundException userNotFoundException(Long id) {
        return new DataNotFoundException(
            messageSource.getMessage("error.not-found-data-with-id", new Object[]{id}, getLocale()));
    }

    private DataNotFoundException technologyNotFoundException() {
        return new DataNotFoundException(messageSource.getMessage("error.technology-not-found", null, getLocale()));
    }
}
