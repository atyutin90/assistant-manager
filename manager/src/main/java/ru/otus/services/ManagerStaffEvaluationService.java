package ru.otus.services;

import ru.otus.dto.ManagerStaffEvaluationDto;

import java.util.List;

public interface ManagerStaffEvaluationService {

    List<ManagerStaffEvaluationDto> findAll();
}
