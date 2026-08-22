package ru.otus.utils;

import lombok.experimental.UtilityClass;
import ru.otus.entity.AssessmentProjectQuestion;
import ru.otus.entity.CareerLevel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Comparator.comparingInt;
import static java.util.List.of;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class CareerLevelCalculator {

    public static CareerLevel calculateCurrentCareerLevel(
        Map<CareerLevel, List<AssessmentProjectQuestion>> questionMapByCareerLevel,
        Set<Long> yesQuestionIds
    ) {
        var result = new AtomicReference<CareerLevel>();
        var finish = new AtomicReference<>(false);
        questionMapByCareerLevel.keySet().stream()
            .sorted(comparingInt(CareerLevel::getPosition))
            .forEach(careerLevel -> {
                var projectQuestions = questionMapByCareerLevel.getOrDefault(careerLevel, of());
                var projectQuestionIds = projectQuestions.stream()
                    .map(it -> it.getQuestion().getId())
                    .collect(toSet());
                if (!finish.get()) {
                    if (yesQuestionIds.containsAll(projectQuestionIds) && !projectQuestionIds.isEmpty()) {
                        result.set(careerLevel);
                    } else if (!yesQuestionIds.containsAll(projectQuestionIds) && !projectQuestionIds.isEmpty())  {
                        finish.set(true);
                    }
                }
            });
        return result.get();
    }
}
