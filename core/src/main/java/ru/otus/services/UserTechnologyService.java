package ru.otus.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.UserTechnologyDto;
import ru.otus.dto.filter.UserTechnologyFilter;
import ru.otus.entity.enums.TechnologyLevel;

import java.util.Set;

public interface UserTechnologyService {

    Page<UserTechnologyDto> findAll(UserTechnologyFilter filter, Pageable pageable);

    void addSelected(Long userId, Set<Long> technologyIds);

    void changeLevel(Long userId, Long technologyId, TechnologyLevel level);

    void delete(Long userId, Long technologyId);
}
