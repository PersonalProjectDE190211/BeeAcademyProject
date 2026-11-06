package com.beeacademy.backend.infrastructure.course.persistence.repository;

import com.beeacademy.backend.infrastructure.course.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository cho bảng 'categories'.
 */
@Repository
public interface SpringDataCategoryRepository extends JpaRepository<CategoryJpaEntity, UUID> {

    Optional<CategoryJpaEntity> findBySlug(String slug);

    List<CategoryJpaEntity> findByActiveTrueOrderByDisplayOrderAsc();

    boolean existsBySlug(String slug);
}
