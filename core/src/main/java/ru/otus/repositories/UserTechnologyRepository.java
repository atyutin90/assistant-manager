package ru.otus.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.otus.entity.UserTechnology;

import javax.annotation.Nullable;
import java.util.Optional;

import static ru.otus.entity.UserTechnology.USER_TECHNOLOGY_GRAPH;

@Repository
public interface UserTechnologyRepository extends JpaRepository<UserTechnology, Long>,
    JpaSpecificationExecutor<UserTechnology> {

    @EntityGraph(value = USER_TECHNOLOGY_GRAPH)
    @Override
    Page<UserTechnology> findAll(@Nullable Specification<UserTechnology> spec, Pageable pageable);

    Optional<UserTechnology> findByTechnologyIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndTechnologyId(Long userId, Long technologyId);

    void deleteByTechnologyId(Long technologyId);
}
