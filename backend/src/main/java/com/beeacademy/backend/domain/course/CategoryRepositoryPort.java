package com.beeacademy.backend.domain.course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Danh mục môn học.
 */
public interface CategoryRepositoryPort {

    Category save(Category category);

    Optional<Category> findById(UUID id);

    Optional<Category> findBySlug(String slug);

    List<Category> findAllActive();

    boolean existsBySlug(String slug);
}
