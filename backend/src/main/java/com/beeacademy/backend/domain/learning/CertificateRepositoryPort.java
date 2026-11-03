package com.beeacademy.backend.domain.learning;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cổng Repository lưu trữ Chứng chỉ hoàn thành khóa học (Certificate).
 */
public interface CertificateRepositoryPort {

    Certificate save(Certificate certificate);

    Optional<Certificate> findById(UUID id);

    Optional<Certificate> findByCertificateCode(String certificateCode);

    Optional<Certificate> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<Certificate> findByStudentId(UUID studentId);

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
}
