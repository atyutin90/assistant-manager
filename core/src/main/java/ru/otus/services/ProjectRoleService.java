package ru.otus.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.IdAndValue;
import ru.otus.dto.ProjectRoleDto;

import java.util.List;

public interface ProjectRoleService {

    Page<ProjectRoleDto> findAll(Pageable pageable);

    List<ProjectRoleDto> findAll();

    List<IdAndValue> findAllValues();

    List<IdAndValue> findAllEnabledValues();

    ProjectRoleDto findById(Long id);

    ProjectRoleDto create(ProjectRoleDto data);

    ProjectRoleDto update(ProjectRoleDto data);

    void deleteById(Long id);
}
