package ru.otus.services.survey;

import ru.otus.dto.FeedbackFormDto;
import ru.otus.dto.SurveyAnswerCommand;
import ru.otus.dto.SurveyPageDto;

public interface SurveyService {

    String findStartQuestionUuid(Long staffEvaluationId, Long userId, String projectRole);

    SurveyPageDto findQuestion(Long staffEvaluationId, Long userId, String projectRole, String uuid);

    void saveAnswer(SurveyAnswerCommand command);

    void complete(Long staffEvaluationId, Long userId, String projectRole);

    FeedbackFormDto findFeedback(Long staffEvaluationUserId, Long userId);

    void saveFeedback(Long staffEvaluationUserId, Long userId, FeedbackFormDto feedback, boolean finish);
}
