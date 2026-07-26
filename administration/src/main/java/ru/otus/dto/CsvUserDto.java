package ru.otus.dto;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

import java.util.Set;

@With
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CsvUserDto {
    @CsvBindByName(column = "username", required = true)
    private String username;

    @CsvBindByName(column = "last-name", required = true)
    private String lastName;

    @CsvBindByName(column = "middle-name")
    private String middleName;

    @CsvBindByName(column = "first-name", required = true)
    private String firstName;

    @CsvBindByName(column = "project-role")
    private String projectRole;

    @CsvBindByName(column = "current-level")
    private String currentLevel;

    @CsvBindByName(column = "labor-code-position")
    private String laborCodePosition;

    @CsvBindAndSplitByName(column = "user-roles", elementType = String.class)
    private Set<String> userRoles;

    @CsvBindByName(column = "email")
    private String email;

    @CsvBindByName(column = "password")
    private String password;

    @CsvBindByName(column = "responsible-username")
    private String responsibleUsername;
}
