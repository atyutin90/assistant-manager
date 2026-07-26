package ru.otus.services.staffevaluationquestion;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.otus.dto.StaffEvaluationQuestionDto;
import ru.otus.dto.filter.StaffEvaluationQuestionFilter;
import ru.otus.entity.StaffEvaluationQuestion;
import ru.otus.repositories.StaffEvaluationQuestionRepository;

import static ru.otus.dto.filter.specification.StaffEvaluationQuestionSpecification.staffEvaluationQuestionFilterSpecification;

@Service
@RequiredArgsConstructor
public class StaffEvaluationQuestionServiceImpl implements StaffEvaluationQuestionService {

    private final StaffEvaluationQuestionRepository repository;

    @Override
    public Page<StaffEvaluationQuestionDto> findAll(StaffEvaluationQuestionFilter filter, Pageable pageable) {
        return repository.findAll(staffEvaluationQuestionFilterSpecification(filter), pageable).map(this::dtoOf);
    }

    private StaffEvaluationQuestionDto dtoOf(StaffEvaluationQuestion assignment) {
        var question = assignment.getQuestion();
        return StaffEvaluationQuestionDto.builder()
            .id(question.getId())
            .enabled(question.getEnabled())
            .uuid(question.getUuid())
            .projectRole(question.getProjectRole() != null ? question.getProjectRole().getId() : null)
            .skill(question.getSkill() != null ? question.getSkill().getId() : null)
            .areaKnowledge(question.getAreaKnowledge())
            .section(question.getSection())
            .text(question.getText())
            .position(assignment.getPosition())
            .build();
    }
}
