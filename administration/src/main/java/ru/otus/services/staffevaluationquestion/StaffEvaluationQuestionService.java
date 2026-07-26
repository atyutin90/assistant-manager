package ru.otus.services.staffevaluationquestion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.StaffEvaluationQuestionDto;
import ru.otus.dto.filter.StaffEvaluationQuestionFilter;

public interface StaffEvaluationQuestionService {

    Page<StaffEvaluationQuestionDto> findAll(StaffEvaluationQuestionFilter filter, Pageable pageable);
}
