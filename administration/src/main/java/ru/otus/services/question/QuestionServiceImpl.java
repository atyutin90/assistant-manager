package ru.otus.services.question;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.converters.QuestionDtoConverter;
import ru.otus.dto.QuestionDto;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.exceptions.DataNotFoundException;
import ru.otus.exceptions.NonUniqueValueException;
import ru.otus.repositories.QuestionRepository;

import java.util.HashMap;
import java.util.Map;

import static ru.otus.converters.QuestionDtoConverter.of;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static ru.otus.converters.QuestionDtoConverter.dtoOf;
import static ru.otus.dto.filter.specification.QuestionSpecification.questionFilterSpecification;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    private final MessageSource messageSource;

    @Override
    public Page<QuestionDto> findAll(QuestionFilter filter, Pageable pageable) {
        var questionSpecification = questionFilterSpecification(filter);
        return questionRepository.findAll(questionSpecification, pageable)
            .map(QuestionDtoConverter::dtoOf);
    }

    @Override
    public QuestionDto findById(Long id) {
        return questionRepository.findById(id)
            .map(QuestionDtoConverter::dtoOf)
            .orElseThrow(() -> notFoundException(id));
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }

    @Transactional
    @Override
    public QuestionDto create(QuestionDto data) {
        validate(data);
        var question = questionRepository.save(of(data));
        return dtoOf(question);
    }

    @Transactional
    @Override
    public QuestionDto update(QuestionDto data) {
        boolean result = questionRepository.existsById(data.id());
        if (!result) {
            throw notFoundException(data.id());
        } else {
            validate(data);
            return dtoOf(questionRepository.save(of(data)));
        }
    }

    private DataNotFoundException notFoundException(Long id) {
        return new DataNotFoundException(
            messageSource.getMessage("error.not-found-data-with-id", new Object[]{id}, getLocale()));
    }

    private void validate(QuestionDto data) {
        Map<String, String> map = new HashMap<>();
        if (questionRepository.existsByUuidAndIdNot(data.uuid(),  data.id())) {
            map.put("uuid", messageSource.getMessage("error.non-unique-value", null, getLocale()));
        }
        if (!map.isEmpty()) {
            throw new NonUniqueValueException(map);
        }
    }
}
