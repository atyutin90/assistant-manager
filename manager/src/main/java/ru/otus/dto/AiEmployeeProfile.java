package ru.otus.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AiEmployeeProfile(
    Long id,
    String username,
    List<String> confirmedAnswers,
    List<AiEmployeeTechnology> selfReportedTechnologies
) {
}
