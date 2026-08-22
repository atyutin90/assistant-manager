package ru.otus.dto;

import lombok.Builder;

import java.util.List;

import static ru.otus.entity.enums.AnswerResponse.YES;

@Builder
public record ManagerCareerLevelAnswersDto(
    Long careerLevelId,
    String careerLevel,
    List<ManagerEmployeeAnswerDto> answers
) {

    public int successfulAnswerCount() {
        return (int) answers.stream()
            .filter(answer -> YES == answer.response() && YES == answer.verifiedResponse())
            .count();
    }

    public int unsuccessfulAnswerCount() {
        return answers.size() - successfulAnswerCount();
    }
}
