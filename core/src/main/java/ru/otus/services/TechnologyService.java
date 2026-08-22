package ru.otus.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.IdAndValue;
import ru.otus.dto.TechnologyDto;
import ru.otus.dto.filter.TechnologyFilter;

import java.util.List;

public interface TechnologyService {

    Page<TechnologyDto> findAll(TechnologyFilter filter, Pageable pageable);

    List<TechnologyDto> findAll();

    List<IdAndValue> findAllValues();

    List<IdAndValue> findAllEnabledValues();

    TechnologyDto findById(Long id);

    TechnologyDto create(TechnologyDto data);

    TechnologyDto update(TechnologyDto data);

    void deleteById(Long id);
}
