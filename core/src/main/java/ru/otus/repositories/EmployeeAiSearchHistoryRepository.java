package ru.otus.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.otus.entity.EmployeeSearchHistory;

import java.util.List;

@Repository
public interface EmployeeAiSearchHistoryRepository extends JpaRepository<EmployeeSearchHistory, Long> {

    List<EmployeeSearchHistory> findByManagerIdOrderByCreatedAtDescIdDesc(Long managerId, Pageable pageable);

    void deleteByManagerId(Long managerId);

    @Modifying
    @Query(value = """
        DELETE FROM employee_search_history
        WHERE manager_id = :managerId
          AND id NOT IN (
              SELECT id
              FROM (
                  SELECT id
                  FROM employee_search_history
                  WHERE manager_id = :managerId
                  ORDER BY created_at DESC, id DESC
                  LIMIT :historySize
              ) visible
          )
        """, nativeQuery = true)
    void deleteOlderThanLimit(
        @Param("managerId") Long managerId,
        @Param("historySize") int historySize
    );
}
