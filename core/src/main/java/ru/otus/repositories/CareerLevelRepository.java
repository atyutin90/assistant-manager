package ru.otus.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.otus.entity.CareerLevel;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareerLevelRepository extends JpaRepository<CareerLevel, Long> {

    Optional<CareerLevel> findByCodeIgnoreCase(String code);

    List<CareerLevel> findAllByOrderByPositionAsc();

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByPositionAndIdNot(Integer position, Long id);
}
