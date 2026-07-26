package ru.otus.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.otus.dto.IdAndValue;
import ru.otus.dto.UserDto;
import ru.otus.dto.filter.UserFilter;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Optional<UserDto> findByUsername(String username);

    Page<UserDto> findAll(UserFilter filter, Pageable pageable);

    UserDto findById(Long id);

    List<IdAndValue> findTeamLeads(Long excludedUserId);

    void deleteById(Long id);

    UserDto create(UserDto data);

    UserDto update(UserDto data);

    void changePassword(Long id, String newPassword);
}
