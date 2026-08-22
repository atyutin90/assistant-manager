package ru.otus.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.converters.TechnologyConverter;
import ru.otus.dto.IdAndValue;
import ru.otus.dto.TechnologyDto;
import ru.otus.dto.filter.TechnologyFilter;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.repositories.TechnologyRepository;
import ru.otus.repositories.UserTechnologyRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.converters.TechnologyConverter.dtoOf;
import static ru.otus.dto.filter.specification.TechnologySpecification.technologyFilterSpecification;

@Slf4j
@Service
@RequiredArgsConstructor
public class TechnologyServiceImpl implements TechnologyService {

    private final TechnologyRepository technologyRepository;

    private final UserTechnologyRepository userTechnologyRepository;

    private final MessageSource messageSource;

    @Override
    public Page<TechnologyDto> findAll(TechnologyFilter filter, Pageable pageable) {
        var technologySpecification = technologyFilterSpecification(filter);
        return technologyRepository.findAll(technologySpecification, pageable)
            .map(TechnologyConverter::dtoOf);
    }

    @Override
    public List<TechnologyDto> findAll() {
        return technologyRepository.findAllByOrderByNameAsc().stream()
            .map(TechnologyConverter::dtoOf)
            .toList();
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
            .filter(TechnologyDto::enabled)
            .map(it -> IdAndValue.builder().id(it.id()).value(it.name()).build())
            .toList();
    }

    @Override
    public TechnologyDto findById(Long id) {
        return technologyRepository.findById(id)
            .map(TechnologyConverter::dtoOf)
            .orElseThrow(() -> notFoundException(id));
    }

    @Transactional
    @Override
    public TechnologyDto create(TechnologyDto data) {
        validate(data);
        return dtoOf(technologyRepository.save(TechnologyConverter.of(data)));
    }

    @Transactional
    @Override
    public TechnologyDto update(TechnologyDto data) {
        boolean result = technologyRepository.existsById(data.id());
        if (!result) {
            throw notFoundException(data.id());
        }
        validate(data);
        return dtoOf(technologyRepository.save(TechnologyConverter.of(data)));
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        userTechnologyRepository.deleteByTechnologyId(id);
        technologyRepository.deleteById(id);
    }

    private void validate(TechnologyDto data) {
        Map<String, String> map = new HashMap<>();
        if (technologyRepository.existsByNameAndIdNot(data.name(),  data.id())) {
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
