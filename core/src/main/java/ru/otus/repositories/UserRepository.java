package ru.otus.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.otus.entity.User;
import ru.otus.entity.enums.UserRole;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ru.otus.entity.User.USER_ALL_GRAPH;
import static ru.otus.entity.User.USER_GRAPH;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @EntityGraph(value = USER_GRAPH)
    @Override
    Page<User> findAll(@Nullable Specification<User> spec, Pageable pageable);

    @EntityGraph(value = USER_ALL_GRAPH)
    @Override
    List<User> findAll(@Nullable Specification<User> spec);

    @EntityGraph(value = USER_ALL_GRAPH)
    @Override
    Optional<User> findById(Long id);

    @EntityGraph(value = USER_ALL_GRAPH)
    Optional<User> findByUsername(String username);

    @EntityGraph(value = USER_ALL_GRAPH)
    List<User> findByRolesContainsAndIdNotIn(UserRole role, Set<Long> excludedUserIds);

    @EntityGraph(value = USER_ALL_GRAPH)
    Optional<User> findByRolesContainsAndUsername(UserRole role, String username);

    @EntityGraph(value = USER_ALL_GRAPH)
    List<User> findByRolesContains(UserRole role);

    boolean existsByUsernameAndIdNot(String uuid, Long id);
}
