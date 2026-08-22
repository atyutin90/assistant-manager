package ru.otus.controllers.pages;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.annotations.CurrentUserParam;
import ru.otus.dto.CurrentUser;
import ru.otus.dto.FeedbackFormDto;
import ru.otus.dto.SurveyAnswerCommand;
import ru.otus.dto.SurveyAnswerDto;
import ru.otus.dto.SurveyPageDto;
import ru.otus.services.survey.SurveyService;

@Controller
@RequiredArgsConstructor
public class SurveyPageController implements AbstractPageController {

    private static final String SURVEY = "survey";

    private static final String ANSWER = "answer";

    private static final String FEEDBACK = "feedback";

    private final SurveyService surveyService;

    @GetMapping("/survey/{staffEvaluationId}/{projectRole}")
    public String startSurvey(
        @PathVariable Long staffEvaluationId,
        @PathVariable String projectRole,
        @CurrentUserParam CurrentUser currentUser
    ) {
        var uuid = surveyService.findStartQuestionUuid(staffEvaluationId, currentUser.id(), projectRole);
        return questionRedirect(staffEvaluationId, projectRole, uuid);
    }

    @GetMapping("/survey/{staffEvaluationId}/{projectRole}/{uuid}")
    public String question(
        @PathVariable Long staffEvaluationId,
        @PathVariable String projectRole,
        @PathVariable String uuid,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        var survey = surveyService.findQuestion(staffEvaluationId, currentUser.id(), projectRole, uuid);
        model.addAttribute(SURVEY, survey);
        model.addAttribute(ANSWER, answerOf(survey));
        return "page/survey/question";
    }

    @PostMapping("/survey/{staffEvaluationId}/{projectRole}/{uuid}")
    public String saveAnswer(
        @PathVariable Long staffEvaluationId,
        @PathVariable String projectRole,
        @PathVariable String uuid,
        @Valid @ModelAttribute(ANSWER) SurveyAnswerDto answer,
        BindingResult bindingResult,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            var question = surveyService.findQuestion(staffEvaluationId, currentUser.id(), projectRole, uuid);
            model.addAttribute(SURVEY, question);
            return "page/survey/question";
        }
        surveyService.saveAnswer(
            SurveyAnswerCommand.builder()
                .staffEvaluationId(staffEvaluationId)
                .projectRole(projectRole)
                .questionUuid(uuid)
                .userId(currentUser.id())
                .response(answer.response())
                .build());
        return surveyRedirect(staffEvaluationId, projectRole);
    }

    @PostMapping("/survey/{staffEvaluationUserId}/{projectRole}/complete")
    public String complete(
        @PathVariable Long staffEvaluationUserId,
        @CurrentUserParam CurrentUser currentUser,
        @PathVariable String projectRole
    ) {
        surveyService.complete(staffEvaluationUserId, currentUser.id(), projectRole);
        return "redirect:/staff-evaluations";
    }

    @GetMapping("/survey/{staffEvaluationId}/feedback")
    public String feedback(@PathVariable Long staffEvaluationId,
                           @CurrentUserParam CurrentUser currentUser,
                           Model model) {
        model.addAttribute(FEEDBACK, surveyService.findFeedback(staffEvaluationId, currentUser.id()));
        model.addAttribute("staffEvaluationId", staffEvaluationId);
        return "page/survey/feedback";
    }

    @PostMapping("/survey/{staffEvaluationId}/feedback")
    public String saveFeedback(
        @PathVariable Long staffEvaluationId,
        @RequestParam String action,
        @Valid @ModelAttribute(FEEDBACK) FeedbackFormDto feedback,
        BindingResult bindingResult,
        @CurrentUserParam CurrentUser currentUser,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("staffEvaluationId", staffEvaluationId);
            return "page/survey/feedback";
        }
        surveyService.saveFeedback(staffEvaluationId, currentUser.id(), feedback, FINISH.equals(action));
        return "redirect:/staff-evaluations";
    }


    private SurveyAnswerDto answerOf(SurveyPageDto survey) {
        return SurveyAnswerDto.builder().response(survey.currentQuestion().response()).build();
    }

    private String surveyRedirect(Long evaluationId, String projectRole) {
        return "redirect:/survey/%d/%s".formatted(evaluationId, projectRole);
    }

    private String questionRedirect(Long evaluationId, String projectRole, String questionUuid) {
        return "%s/%s".formatted(surveyRedirect(evaluationId, projectRole), questionUuid);
    }
}
