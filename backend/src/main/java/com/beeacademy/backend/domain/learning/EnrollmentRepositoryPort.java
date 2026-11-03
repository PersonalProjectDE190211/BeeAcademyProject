package com.beeacademy.backend.domain.learning;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ thông tin Ghi danh học tập (Enrollment).
 */
public interface EnrollmentRepositoryPort {

    Enrollment save(Enrollment enrollment);

    Optional<Enrollment> findById(UUID id);

    Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<Enrollment> findByStudentId(UUID studentId);

    List<Enrollment> findByCourseId(UUID courseId);

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
}
