package com.beeacademy.backend.application.course.usecase;

import com.beeacademy.backend.application.course.dto.CategoryResult;
import com.beeacademy.backend.domain.course.CategoryRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case Lấy danh mục môn học đang hoạt động.
 */
@Service
public class ListCategoriesUseCase {

    private final CategoryRepositoryPort categoryRepository;

    public ListCategoriesUseCase(CategoryRepositoryPort categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResult> execute() {
        return categoryRepository.findAllActive().stream()
                .map(CategoryResult::from)
                .toList();
    }
}

