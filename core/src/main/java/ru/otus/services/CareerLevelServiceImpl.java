package ru.otus.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.converters.CareerLevelConverter;
import ru.otus.dto.CareerLevelDto;
import ru.otus.dto.IdAndValue;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.repositories.CareerLevelRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.converters.CareerLevelConverter.dtoOf;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareerLevelServiceImpl implements CareerLevelService {

    private final CareerLevelRepository careerLevelRepository;

    private final MessageSource messageSource;

    @Override
    public Page<CareerLevelDto> findAll(Pageable pageable) {
        return careerLevelRepository.findAll(pageable)
            .map(CareerLevelConverter::dtoOf);
    }

    @Override
    public List<CareerLevelDto> findAll() {
        return careerLevelRepository.findAllByOrderByPositionAsc().stream()
            .map((CareerLevelConverter::dtoOf)).toList();
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
            .filter(CareerLevelDto::enabled)
            .map(it ->
                IdAndValue.builder()
                    .id(it.id())
                    .value(it.name())
                    .build())
            .toList();
    }

    @Override
    public CareerLevelDto findById(Long id) {
        return careerLevelRepository.findById(id)
            .map(CareerLevelConverter::dtoOf)
            .orElseThrow(() -> notFoundException(id));
    }

    @Transactional
    @Override
    public CareerLevelDto create(CareerLevelDto data) {
        validate(data);
        return dtoOf(careerLevelRepository.save(CareerLevelConverter.of(data)));
    }

    @Transactional
    @Override
    public CareerLevelDto update(CareerLevelDto data) {
        boolean result = careerLevelRepository.existsById(data.id());
        if (!result) {
            throw notFoundException(data.id());
        }
        validate(data);
        return dtoOf(careerLevelRepository.save(CareerLevelConverter.of(data)));
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        careerLevelRepository.deleteById(id);
    }

    private void validate(CareerLevelDto data) {
        Map<String, String> map = new HashMap<>();
        if (careerLevelRepository.existsByCodeAndIdNot(data.code(),  data.id())) {
            map.put("code", messageSource.getMessage("error.non-unique-value", null, getLocale()));
        }
        if (careerLevelRepository.existsByNameAndIdNot(data.name(),  data.id())) {
            map.put("name", messageSource.getMessage("error.non-unique-value", null, getLocale()));
        }
        if (careerLevelRepository.existsByPositionAndIdNot(data.position(),  data.id())) {
            map.put("position", messageSource.getMessage("error.non-unique-value", null, getLocale()));
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
