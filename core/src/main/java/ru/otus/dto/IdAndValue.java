package ru.otus.dto;

import lombok.Builder;

@Builder
public record IdAndValue(Long id, String value) { }
