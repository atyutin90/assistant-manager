package ru.otus.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@With
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CsvQuestionDto {

    @CsvBindByName(column = "enabled", required = true)
    private Boolean enabled;

    @CsvBindByName(column = "uuid", required = true)
    private String uuid;

    @CsvBindByName(column = "project-role", required = true)
    private String projectRole;

    @CsvBindByName(column = "skill", required = true)
    private String skill;

    @CsvBindByName(column = "area-knowledge")
    private String areaKnowledge;

    @CsvBindByName(column = "section")
    private String section;

    @CsvBindByName(column = "text", required = true)
    private String text;
}
