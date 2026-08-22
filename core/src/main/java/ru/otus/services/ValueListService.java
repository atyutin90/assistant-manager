package ru.otus.services;

import ru.otus.dto.CodeAndValue;

import java.util.List;

public interface ValueListService {

    List<CodeAndValue> getValues(String type);
}
