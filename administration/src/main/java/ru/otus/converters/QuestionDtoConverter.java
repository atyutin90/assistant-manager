package ru.otus.converters;

import ru.otus.dto.QuestionDto;
import ru.otus.entity.ProjectRole;
import ru.otus.entity.Question;
import ru.otus.entity.Skill;

import static java.util.Optional.ofNullable;

public class QuestionDtoConverter {

    public static QuestionDto dtoOf(Question data) {
        return QuestionDto.builder()
            .id(data.getId())
            .enabled(data.getEnabled())
            .uuid(data.getUuid())
            .projectRole(data.getProjectRole() != null ? data.getProjectRole().getId() : null)
            .skill(data.getSkill() != null ? data.getSkill().getId() : null)
            .areaKnowledge(data.getAreaKnowledge())
            .section(data.getSection())
            .text(data.getText())
            .build();
    }

    public static Question of(QuestionDto data) {
        return Question.builder()
            .id(data.id())
            .enabled(data.enabled())
            .uuid(data.uuid())
            .projectRole(ofNullable(data.projectRole()).map(it -> ProjectRole.builder().id(it).build()).orElse(null))
            .skill(ofNullable(data.skill()).map(it -> Skill.builder().id(it).build()).orElse(null))
            .areaKnowledge(data.areaKnowledge())
            .section(data.section())
            .text(data.text())
            .build();
    }
}
