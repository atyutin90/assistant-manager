package ru.otus.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProjectQuestionsForm(
    List<ProjectQuestionLevelDto> questions
) {

}
