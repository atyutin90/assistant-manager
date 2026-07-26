package ru.otus.services.staffevaluation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.filter.QuestionFilter;
import ru.otus.dto.StaffEvaluationEmployeeDto;
import ru.otus.dto.StaffEvaluationQuestionsDto;
import ru.otus.dto.StaffEvaluationDto;
import ru.otus.dto.filter.StaffEvaluationFilter;
import ru.otus.dto.filter.UserFilter;

import java.util.Map;
import java.util.Set;

public interface StaffEvaluationService {

    Page<StaffEvaluationDto> findAll(StaffEvaluationFilter filter, Pageable pageable);

    StaffEvaluationDto findById(Long id);

    StaffEvaluationDto create(StaffEvaluationDto data);

    StaffEvaluationDto update(StaffEvaluationDto data);

    void deleteById(Long id);

    void start(Long id);

    void complete(Long id);


    StaffEvaluationEmployeeDto findEmployeeById(Long id);

    void addEmployees(Long staffEvaluationId, Set<Long> employeeIds);

    void addEmployees(Long staffEvaluationId, UserFilter filter);

    void removeEmployees(Long staffEvaluationId, Set<Long> employeeIds);


    StaffEvaluationQuestionsDto findQuestionsById(Long id);

    void addQuestions(Long staffEvaluationId, Set<Long> questionIds);

    void addQuestions(Long staffEvaluationId, QuestionFilter filter);

    void removeQuestions(Long staffEvaluationId, Set<Long> questionIds);

    void updateQuestionPositions(Long staffEvaluationId, Map<Long, Integer> questionPositions);
}
