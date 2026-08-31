package ru.otus.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.otus.entity.StaffEvaluationUser;
import ru.otus.entity.enums.StaffEvaluationStatus;
import ru.otus.entity.enums.StaffEvaluationUserStatus;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static ru.otus.entity.StaffEvaluationUser.STAFF_EVALUATION_USER_ALL_GRAPH;
import static ru.otus.entity.StaffEvaluationUser.STAFF_EVALUATION_USER_GRAPH;

@Repository
public interface StaffEvaluationUserRepository extends JpaRepository<StaffEvaluationUser, Long>,
    JpaSpecificationExecutor<StaffEvaluationUser> {

    @EntityGraph(value = STAFF_EVALUATION_USER_GRAPH)
    @Override
    Page<StaffEvaluationUser> findAll(@Nullable Specification<StaffEvaluationUser> spec, Pageable pageable);

    @EntityGraph(value = STAFF_EVALUATION_USER_GRAPH)
    @Override
    List<StaffEvaluationUser> findAll(@Nullable Specification<StaffEvaluationUser> spec);

    @EntityGraph(value = STAFF_EVALUATION_USER_GRAPH)
    Page<StaffEvaluationUser> findByUserIdAndStaffEvaluationStatusNot(
        Long userId,
        StaffEvaluationStatus excludedStatus,
        Pageable pageable
    );

    @Query("""
        select seu
        from StaffEvaluationUser seu
        where seu.id = (
              select max(last.id)
              from StaffEvaluationUser last
              where last.user.id = seu.user.id
                and last.projectRole.id = seu.projectRole.id
                and last.status = :status
          )
        """)
    @EntityGraph(value = STAFF_EVALUATION_USER_ALL_GRAPH)
    List<StaffEvaluationUser> findLastByStatusForAllUsers(StaffEvaluationUserStatus status);

    @EntityGraph(value = STAFF_EVALUATION_USER_ALL_GRAPH)
    List<StaffEvaluationUser> findByStaffEvaluationIdAndStatus(
        Long staffEvaluationId,
        StaffEvaluationUserStatus status
    );

    @EntityGraph(value = STAFF_EVALUATION_USER_GRAPH)
    List<StaffEvaluationUser> findByUserIdAndStaffEvaluationStatus(
        Long userId,
        StaffEvaluationStatus status
    );

    @Query("""
        select seu
        from StaffEvaluationUser seu
        where seu.id = (
              select max(last.id)
              from StaffEvaluationUser last
              where last.user.id = :userId
                and last.projectRole.id = :projectRoleId
                and last.status = :status
          )
        """)
    @EntityGraph(value = STAFF_EVALUATION_USER_ALL_GRAPH)
    Optional<StaffEvaluationUser> findLastByStatusForUserIdAndProjectRoleId(Long userId,
                                                                            Long projectRoleId,
                                                                            StaffEvaluationUserStatus status);

    @EntityGraph(value = STAFF_EVALUATION_USER_ALL_GRAPH)
    Optional<StaffEvaluationUser> findByStaffEvaluationIdAndUserIdAndProjectRoleId(Long staffEvaluationId,
                                                                                   Long userId,
                                                                                   Long projectRoleId);

    @EntityGraph(value = STAFF_EVALUATION_USER_ALL_GRAPH)
    Optional<StaffEvaluationUser> findByStaffEvaluationIdAndUserIdAndProjectRoleCodeIgnoreCase(
        Long staffEvaluationId,
        Long userId,
        String projectRole
    );

    @EntityGraph(value = STAFF_EVALUATION_USER_ALL_GRAPH)
    Optional<StaffEvaluationUser> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(value = STAFF_EVALUATION_USER_GRAPH)
    Page<StaffEvaluationUser> findByUserResponsibleIdAndStatus(
        Long responsibleId,
        StaffEvaluationUserStatus status,
        Pageable pageable
    );

    @EntityGraph(value = STAFF_EVALUATION_USER_GRAPH)
    Optional<StaffEvaluationUser> findByIdAndUserResponsibleIdAndStatus(
        Long id,
        Long responsibleId,
        StaffEvaluationUserStatus status
    );
}
