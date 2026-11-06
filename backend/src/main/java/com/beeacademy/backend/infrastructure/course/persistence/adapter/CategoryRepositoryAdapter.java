package com.beeacademy.backend.infrastructure.course.persistence.adapter;

import com.beeacademy.backend.domain.course.Category;
import com.beeacademy.backend.domain.course.CategoryRepositoryPort;
import com.beeacademy.backend.infrastructure.course.persistence.entity.CategoryJpaEntity;
import com.beeacademy.backend.infrastructure.course.persistence.repository.SpringDataCategoryRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter triển khai Domain Port {@link CategoryRepositoryPort} bằng Spring Data JPA.
 */
@Component
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

    private final SpringDataCategoryRepository repository;

    public CategoryRepositoryAdapter(SpringDataCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Category save(Category category) {
        CategoryJpaEntity entity = toEntity(category);
        CategoryJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return repository.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public List<Category> findAllActive() {
        return repository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySlug(String slug) {
        return repository.existsBySlug(slug);
    }

    private Category toDomain(CategoryJpaEntity entity) {
        return new Category(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getIconName(),
                entity.getDisplayOrder(),
                entity.isActive()
        );
    }

    private CategoryJpaEntity toEntity(Category domain) {
        return new CategoryJpaEntity(
                domain.getId(),
                domain.getName(),
                domain.getSlug(),
                domain.getDescription(),
                domain.getIconUrl(),
                domain.getDisplayOrder(),
                domain.isActive(),
                Instant.now()
        );
    }
}
