package ru.otus.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.otus.entity.AssessmentProject;

import java.util.Optional;

import static ru.otus.entity.AssessmentProject.ASSESSMENT_PROJECT_GRAPH;

@Repository
public interface AssessmentProjectRepository extends
    JpaRepository<AssessmentProject, Long>, JpaSpecificationExecutor<AssessmentProject> {

    @EntityGraph(value = ASSESSMENT_PROJECT_GRAPH)
    @Query("""
        SELECT DISTINCT project
        FROM AssessmentProject project
        LEFT JOIN project.sharedManagers manager
        WHERE project.id = :id AND (project.owner.id = :managerId OR manager.id = :managerId)
        """)
    Optional<AssessmentProject> findAccessibleById(Long id, Long managerId);

    @EntityGraph(value = ASSESSMENT_PROJECT_GRAPH)
    Optional<AssessmentProject> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);

    boolean existsByOwnerIdAndNameIgnoreCaseAndIdNot(Long ownerId, String name, Long id);
}
