package com.beeacademy.backend.infrastructure.course.persistence.repository;

import com.beeacademy.backend.infrastructure.course.persistence.entity.CourseReviewJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository cho bảng 'course_reviews'.
 */
@Repository
public interface SpringDataCourseReviewRepository extends JpaRepository<CourseReviewJpaEntity, UUID> {

    Optional<CourseReviewJpaEntity> findByCourseIdAndStudentId(UUID courseId, UUID studentId);

    List<CourseReviewJpaEntity> findByCourseIdOrderByCreatedAtDesc(UUID courseId);
}
