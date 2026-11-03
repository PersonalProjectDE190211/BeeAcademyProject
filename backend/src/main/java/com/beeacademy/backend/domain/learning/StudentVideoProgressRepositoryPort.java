package com.beeacademy.backend.domain.learning;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Tiến độ xem video bài học (StudentVideoProgress).
 */
public interface StudentVideoProgressRepositoryPort {

    StudentVideoProgress save(StudentVideoProgress progress);

    Optional<StudentVideoProgress> findById(UUID id);

    Optional<StudentVideoProgress> findByStudentIdAndLessonId(UUID studentId, UUID lessonId);

    List<StudentVideoProgress> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    long countCompletedLessons(UUID studentId, UUID courseId);
}
