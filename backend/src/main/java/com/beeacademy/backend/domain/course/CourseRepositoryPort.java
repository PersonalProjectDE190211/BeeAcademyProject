package com.beeacademy.backend.domain.course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ và truy vấn Khóa học.
 */
public interface CourseRepositoryPort {

    Course save(Course course);

    Optional<Course> findById(UUID id);

    Optional<Course> findBySlug(String slug);

    List<Course> findByTeacherId(UUID teacherId);

    List<Course> findPublishedCourses(UUID categoryId, Integer grade);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
