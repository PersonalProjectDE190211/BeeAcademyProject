package com.beeacademy.backend.domain.course;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Đánh giá khóa học.
 */
public interface CourseReviewRepositoryPort {

    CourseReview save(CourseReview review);

    Optional<CourseReview> findById(UUID id);

    Optional<CourseReview> findByCourseIdAndStudentId(UUID courseId, UUID studentId);

    List<CourseReview> findByCourseId(UUID courseId);

    void deleteById(UUID id);
}

