package com.beeacademy.backend.infrastructure.learning.persistence.repository;

import com.beeacademy.backend.infrastructure.learning.persistence.entity.CertificateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCertificateRepository extends JpaRepository<CertificateJpaEntity, UUID> {

    Optional<CertificateJpaEntity> findBySerialNumber(String serialNumber);

    Optional<CertificateJpaEntity> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<CertificateJpaEntity> findByStudentIdOrderByIssuedAtDesc(UUID studentId);

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
}
