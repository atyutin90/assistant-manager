package ru.otus.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.CareerLevelDto;
import ru.otus.dto.IdAndValue;

import java.util.List;

public interface CareerLevelService {

    Page<CareerLevelDto> findAll(Pageable pageable);

    List<CareerLevelDto> findAll();

    List<IdAndValue> findAllValues();

    List<IdAndValue> findAllEnabledValues();

    CareerLevelDto findById(Long id);

    CareerLevelDto create(CareerLevelDto data);

    CareerLevelDto update(CareerLevelDto data);

    void deleteById(Long id);
}
