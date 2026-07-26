package ru.otus.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.converters.ProjectRoleConverter;
import ru.otus.dto.IdAndValue;
import ru.otus.dto.ProjectRoleDto;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.repositories.ProjectRoleRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.converters.ProjectRoleConverter.dtoOf;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectRoleServiceImpl implements ProjectRoleService {

    private final ProjectRoleRepository projectRoleRepository;

    private final MessageSource messageSource;

    @Override
    public Page<ProjectRoleDto> findAll(Pageable pageable) {
        return projectRoleRepository.findAll(pageable)
            .map(ProjectRoleConverter::dtoOf);
    }

    @Override
    public List<ProjectRoleDto> findAll() {
        return projectRoleRepository.findAllByOrderByPositionAsc().stream()
            .map((ProjectRoleConverter::dtoOf)).toList();
    }

    @Override
    public List<IdAndValue> findAllValues() {
        return findAll().stream()
            .map(it ->
                IdAndValue.builder()
                    .id(it.id())
                    .value(format("%s %s", it.name(), it.enabled() ? "" : "❌"))
                    .build()
            ).toList();
    }

    @Override
    public List<IdAndValue> findAllEnabledValues() {
        return findAll().stream()
            .filter(ProjectRoleDto::enabled)
            .map(it -> IdAndValue.builder().id(it.id()).value(it.name()).build())
            .toList();
    }

    @Override
    public ProjectRoleDto findById(Long id) {
        return projectRoleRepository.findById(id)
            .map(ProjectRoleConverter::dtoOf)
            .orElseThrow(() -> notFoundException(id));
    }

    @Transactional
    @Override
    public ProjectRoleDto create(ProjectRoleDto data) {
        validate(data);
        return dtoOf(projectRoleRepository.save(ProjectRoleConverter.of(data)));
    }

    @Transactional
    @Override
    public ProjectRoleDto update(ProjectRoleDto data) {
        boolean result = projectRoleRepository.existsById(data.id());
        if (!result) {
            throw notFoundException(data.id());
        }
        validate(data);
        return dtoOf(projectRoleRepository.save(ProjectRoleConverter.of(data)));
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        projectRoleRepository.deleteById(id);
    }

    private void validate(ProjectRoleDto data) {
        Map<String, String> map = new HashMap<>();
        if (projectRoleRepository.existsByCodeAndIdNot(data.code(),  data.id())) {
            map.put("code", messageSource.getMessage("error.non-unique-value", null, getLocale()));
        }
        if (projectRoleRepository.existsByNameAndIdNot(data.name(),  data.id())) {
            map.put("name", messageSource.getMessage("error.non-unique-value", null, getLocale()));
        }
        if (!map.isEmpty()) {
            throw new NonUniqueValueException(map);
        }
    }

    private DataNotFoundException notFoundException(Long id) {
        return new DataNotFoundException(
            messageSource.getMessage("error.not-found-data-with-id", new Object[]{id}, getLocale())
        );
    }
}
