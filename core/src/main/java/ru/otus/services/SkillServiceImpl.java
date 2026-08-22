package ru.otus.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.converters.SkillConverter;
import ru.otus.dto.IdAndValue;
import ru.otus.dto.SkillDto;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.repositories.SkillRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.converters.SkillConverter.dtoOf;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    private final MessageSource messageSource;

    @Override
    public Page<SkillDto> findAll(Pageable pageable) {
        return skillRepository.findAll(pageable)
            .map(SkillConverter::dtoOf);
    }

    @Override
    public List<SkillDto> findAll() {
        return skillRepository.findAllByOrderByPositionAsc().stream()
            .map(SkillConverter::dtoOf)
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
            .filter(SkillDto::enabled)
            .map(it -> IdAndValue.builder().id(it.id()).value(it.name()).build())
            .toList();
    }

    @Override
    public SkillDto findById(Long id) {
        return skillRepository.findById(id)
            .map(SkillConverter::dtoOf)
            .orElseThrow(() -> notFoundException(id));
    }

    @Transactional
    @Override
    public SkillDto create(SkillDto data) {
        validate(data);
        return dtoOf(skillRepository.save(SkillConverter.of(data)));
    }

    @Transactional
    @Override
    public SkillDto update(SkillDto data) {
        boolean result = skillRepository.existsById(data.id());
        if (!result) {
            throw notFoundException(data.id());
        }
        validate(data);
        return dtoOf(skillRepository.save(SkillConverter.of(data)));
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        skillRepository.deleteById(id);
    }

    private void validate(SkillDto data) {
        Map<String, String> map = new HashMap<>();
        if (skillRepository.existsByCodeAndIdNot(data.code(),  data.id())) {
            map.put("code", messageSource.getMessage("error.non-unique-value", null, getLocale()));
        }
        if (skillRepository.existsByNameAndIdNot(data.name(),  data.id())) {
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
