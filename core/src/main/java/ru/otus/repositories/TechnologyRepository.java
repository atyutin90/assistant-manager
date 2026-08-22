package ru.otus.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.otus.entity.Technology;

import java.util.Collection;
import java.util.List;

@Repository
public interface TechnologyRepository extends JpaRepository<Technology, Long>, JpaSpecificationExecutor<Technology> {

    Page<Technology> findAll(Specification<Technology> spec, Pageable pageable);

    List<Technology> findAllByOrderByNameAsc();

    List<Technology> findByIdInAndEnabledTrue(Collection<Long> id);

    boolean existsByNameAndIdNot(String name, Long id);
}
