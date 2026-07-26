package ru.otus.services.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.QuestionDto;
import ru.otus.dto.filter.QuestionFilter;

public interface QuestionService {

    Page<QuestionDto> findAll(QuestionFilter filter, Pageable pageable);

    QuestionDto findById(Long id);

    void deleteById(Long id);

    QuestionDto create(QuestionDto data);

    QuestionDto update(QuestionDto data);
}
