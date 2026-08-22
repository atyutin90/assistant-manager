package ru.otus.services;

import ru.otus.dto.ProfileDto;

public interface ProfileService {

    ProfileDto getProfile(Long userId);

    void changePassword(Long userId, String newPassword);
}
