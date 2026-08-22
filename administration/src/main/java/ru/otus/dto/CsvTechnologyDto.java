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
public class CsvTechnologyDto {
    @CsvBindByName(column = "name", required = true)
    private String name;
}
