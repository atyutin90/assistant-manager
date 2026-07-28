package ru.otus.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.IdAndValue;
import ru.otus.dto.SkillDto;

import java.util.List;

public interface SkillService {

    Page<SkillDto> findAll(Pageable pageable);

    List<SkillDto> findAll();

    List<IdAndValue> findAllValues();

    List<IdAndValue> findAllEnabledValues();

    SkillDto findById(Long id);

    SkillDto create(SkillDto data);

    SkillDto update(SkillDto data);

    void deleteById(Long id);
}
