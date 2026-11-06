package com.beeacademy.backend.infrastructure.learning.persistence.adapter;

import com.beeacademy.backend.domain.learning.Certificate;
import com.beeacademy.backend.domain.learning.CertificateRepositoryPort;
import com.beeacademy.backend.infrastructure.learning.persistence.entity.CertificateJpaEntity;
import com.beeacademy.backend.infrastructure.learning.persistence.repository.SpringDataCertificateRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter triển khai Domain Port {@link CertificateRepositoryPort} bằng Spring Data JPA.
 */
@Component
public class CertificateRepositoryAdapter implements CertificateRepositoryPort {

    private final SpringDataCertificateRepository repository;

    public CertificateRepositoryAdapter(SpringDataCertificateRepository repository) {
        this.repository = repository;
    }

    @Override
    public Certificate save(Certificate certificate) {
        CertificateJpaEntity entity = toEntity(certificate);
        CertificateJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Certificate> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Certificate> findByCertificateCode(String certificateCode) {
        return repository.findBySerialNumber(certificateCode).map(this::toDomain);
    }

    @Override
    public Optional<Certificate> findByStudentIdAndCourseId(UUID studentId, UUID courseId) {
        return repository.findByStudentIdAndCourseId(studentId, courseId).map(this::toDomain);
    }

    @Override
    public List<Certificate> findByStudentId(UUID studentId) {
        return repository.findByStudentIdOrderByIssuedAtDesc(studentId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId) {
        return repository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    private Certificate toDomain(CertificateJpaEntity entity) {
        return new Certificate(
                entity.getId(),
                entity.getSerialNumber(),
                entity.getStudentId(),
                entity.getCourseId(),
                entity.getStudentNameSnapshot(),
                entity.getCourseTitleSnapshot(),
                entity.getIssuedAt(),
                entity.getPdfUrl()
        );
    }

    private CertificateJpaEntity toEntity(Certificate domain) {
        UUID enrollmentId = UUID.nameUUIDFromBytes((domain.getStudentId().toString() + "-" + domain.getCourseId().toString()).getBytes());
        return new CertificateJpaEntity(
                domain.getId(),
                enrollmentId,
                domain.getStudentId(),
                domain.getCourseId(),
                domain.getCertificateCode(),
                domain.getStudentName(),
                domain.getCourseTitle(),
                BigDecimal.valueOf(100.0), // hoàn thành 100%
                domain.getPdfUrl(),
                domain.getPdfUrl(),
                false,
                domain.getIssuedAt()
        );
    }
}
