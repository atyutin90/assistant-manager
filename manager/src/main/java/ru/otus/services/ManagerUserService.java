package ru.otus.services;

import ru.otus.dto.ManagerUserDto;
import ru.otus.dto.filter.ManagerUserFilter;

import java.util.List;

public interface ManagerUserService {

    List<ManagerUserDto> findAll(ManagerUserFilter filter);
}
