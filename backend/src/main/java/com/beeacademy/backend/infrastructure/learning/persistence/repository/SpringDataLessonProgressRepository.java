package com.beeacademy.backend.infrastructure.learning.persistence.repository;

import com.beeacademy.backend.infrastructure.learning.persistence.entity.LessonProgressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataLessonProgressRepository extends JpaRepository<LessonProgressJpaEntity, UUID> {

    Optional<LessonProgressJpaEntity> findByStudentIdAndLessonId(UUID studentId, UUID lessonId);

    List<LessonProgressJpaEntity> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    @Query("SELECT COUNT(lp) FROM LessonProgressJpaEntity lp WHERE lp.studentId = :studentId AND lp.courseId = :courseId AND lp.isCompleted = true")
    long countCompletedLessons(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);
}
