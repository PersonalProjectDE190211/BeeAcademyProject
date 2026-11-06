package com.beeacademy.backend.application.course.usecase;

import com.beeacademy.backend.application.course.dto.CourseSummaryResult;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Use case Tìm kiếm và Xem danh mục khóa học đã xuất bản (UC06).
 */
@Service
public class BrowseCatalogUseCase {

    private final CourseRepositoryPort courseRepository;

    public BrowseCatalogUseCase(CourseRepositoryPort courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryResult> search(
            String keyword,
            UUID categoryId,
            Integer grade,
            Integer minPrice,
            Integer maxPrice,
            String sortBy
    ) {
        List<Course> courses = courseRepository.findPublishedCourses(categoryId, grade);
        Stream<Course> stream = courses.stream();

        // 1. Lọc theo từ khóa (Keyword match trong title hoặc description)
        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.trim().toLowerCase();
            stream = stream.filter(c ->
                    (c.getTitle() != null && c.getTitle().toLowerCase().contains(lowerKeyword)) ||
                    (c.getDescription() != null && c.getDescription().toLowerCase().contains(lowerKeyword))
            );
        }

        // 2. Lọc theo khoảng giá (minPrice, maxPrice)
        if (minPrice != null && minPrice >= 0) {
            stream = stream.filter(c -> c.getPrice().amount() >= minPrice);
        }
        if (maxPrice != null && maxPrice >= 0) {
            stream = stream.filter(c -> c.getPrice().amount() <= maxPrice);
        }

        // 3. Sắp xếp kết quả (Sort)
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            stream = stream.sorted(Comparator.comparingInt(c -> c.getPrice().amount()));
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            stream = stream.sorted(Comparator.comparingInt((Course c) -> c.getPrice().amount()).reversed());
        } else if ("rating".equalsIgnoreCase(sortBy)) {
            stream = stream.sorted(Comparator.comparing(Course::getAverageRating).reversed());
        } else {
            // Mặc định: Mới nhất (created/published)
            stream = stream.sorted(Comparator.comparing(Course::getCreatedAt).reversed());
        }

        return stream.map(CourseSummaryResult::from).toList();
    }
}
