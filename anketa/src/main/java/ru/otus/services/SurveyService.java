package ru.otus.services;

import ru.otus.dto.FeedbackFormDto;
import ru.otus.dto.SurveyAnswerCommand;
import ru.otus.dto.SurveyPageDto;

public interface SurveyService {

    String findStartQuestionUuid(Long staffEvaluationId, Long userId, String projectRole);

    SurveyPageDto findQuestion(Long staffEvaluationId, Long userId, String projectRole, String uuid);

    void saveAnswer(SurveyAnswerCommand command);

    void complete(Long staffEvaluationUserId, Long userId, String projectRole);

    FeedbackFormDto findFeedback(Long staffEvaluationId, Long userId);

    void saveFeedback(Long staffEvaluationId, Long userId, FeedbackFormDto feedback, boolean finish);
}
