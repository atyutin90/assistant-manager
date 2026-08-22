package ru.otus.dto;

import lombok.Builder;

@Builder
public record CodeAndValue(String code, String value) { }
