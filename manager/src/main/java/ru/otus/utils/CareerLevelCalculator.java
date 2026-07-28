package ru.otus.utils;

import lombok.experimental.UtilityClass;
import ru.otus.entity.AssessmentProjectQuestion;
import ru.otus.entity.CareerLevel;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.List.of;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class CareerLevelCalculator {

    public static CareerLevel calculateCurrentCareerLevel(
        Map<CareerLevel, List<AssessmentProjectQuestion>> questionMapByCareerLevel,
        Set<Long> yesQuestionIds
    ) {
        var result = new AtomicReference<CareerLevel>();
        questionMapByCareerLevel.keySet().stream()
            .sorted(Comparator.comparingInt(CareerLevel::getPosition))
            .forEach(careerLevel -> {
                var value = result.get();
                var projectQuestions = questionMapByCareerLevel.getOrDefault(careerLevel, of());
                var projectQuestionIds = projectQuestions.stream()
                    .map(it -> it.getQuestion().getId())
                    .collect(toSet());
                if (check(careerLevel, value, projectQuestionIds)) {
                    if (yesQuestionIds.containsAll(projectQuestionIds)) {
                        result.set(careerLevel);
                    }
                }
            });
        return result.get();
    }

    private static boolean check(CareerLevel next, CareerLevel current, Set<Long> projectQuestionIds) {
        return current == null || (next.getPosition() - current.getPosition() <= 1 || projectQuestionIds.isEmpty());
    }
}
