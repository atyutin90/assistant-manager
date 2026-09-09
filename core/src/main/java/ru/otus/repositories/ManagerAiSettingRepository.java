package ru.otus.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.otus.entity.ManagerAiSetting;

import java.util.Optional;

@Repository
public interface ManagerAiSettingRepository extends JpaRepository<ManagerAiSetting, Long> {

    Optional<ManagerAiSetting> findByManagerId(Long managerId);

    boolean existsByManagerUsername(String managerUsername);
}
