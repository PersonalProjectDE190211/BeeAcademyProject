package com.beeacademy.backend.infrastructure.course.persistence.repository;

import com.beeacademy.backend.infrastructure.course.persistence.entity.CourseJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository cho bảng 'courses'.
 */
@Repository
public interface SpringDataCourseRepository extends JpaRepository<CourseJpaEntity, UUID> {

    @Query("SELECT c FROM CourseJpaEntity c LEFT JOIN FETCH c.chapters ch LEFT JOIN FETCH ch.lessons WHERE c.id = :id")
    Optional<CourseJpaEntity> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT c FROM CourseJpaEntity c LEFT JOIN FETCH c.chapters ch LEFT JOIN FETCH ch.lessons WHERE c.slug = :slug")
    Optional<CourseJpaEntity> findBySlugWithDetails(@Param("slug") String slug);

    Optional<CourseJpaEntity> findBySlug(String slug);

    List<CourseJpaEntity> findByTeacherIdOrderByCreatedAtDesc(UUID teacherId);

    List<CourseJpaEntity> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT c FROM CourseJpaEntity c WHERE c.status = 'published' AND (:categoryId IS NULL OR c.categoryId = :categoryId) ORDER BY c.createdAt DESC")
    List<CourseJpaEntity> findPublishedCourses(@Param("categoryId") UUID categoryId);

    long countByStatus(String status);
}
