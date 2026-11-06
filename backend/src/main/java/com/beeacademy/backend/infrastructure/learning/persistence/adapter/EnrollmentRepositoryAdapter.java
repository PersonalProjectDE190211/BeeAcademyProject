package com.beeacademy.backend.infrastructure.learning.persistence.adapter;

import com.beeacademy.backend.domain.common.ScorePercent;
import com.beeacademy.backend.domain.learning.Enrollment;
import com.beeacademy.backend.domain.learning.EnrollmentRepositoryPort;
import com.beeacademy.backend.infrastructure.learning.persistence.entity.EnrollmentJpaEntity;
import com.beeacademy.backend.infrastructure.learning.persistence.repository.SpringDataEnrollmentRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter triển khai Domain Port {@link EnrollmentRepositoryPort} bằng Spring Data JPA.
 */
@Component
public class EnrollmentRepositoryAdapter implements EnrollmentRepositoryPort {

    private final SpringDataEnrollmentRepository repository;

    public EnrollmentRepositoryAdapter(SpringDataEnrollmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        EnrollmentJpaEntity entity = toEntity(enrollment);
        EnrollmentJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Enrollment> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId) {
        return repository.findByStudentIdAndCourseId(studentId, courseId).map(this::toDomain);
    }

    @Override
    public List<Enrollment> findByStudentId(UUID studentId) {
        return repository.findByStudentIdOrderByEnrolledAtDesc(studentId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Enrollment> findByCourseId(UUID courseId) {
        return repository.findByCourseId(courseId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId) {
        return repository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    private Enrollment toDomain(EnrollmentJpaEntity entity) {
        return new Enrollment(
                entity.getId(),
                entity.getStudentId(),
                entity.getCourseId(),
                null, // orderId (can be linked with Commerce module)
                ScorePercent.of(entity.getProgressPercent()),
                entity.getCompletedAt() != null,
                entity.getCompletedAt(),
                entity.getUpdatedAt(),
                entity.getEnrolledAt()
        );
    }

    private EnrollmentJpaEntity toEntity(Enrollment domain) {
        BigDecimal progressBd = domain.getProgressPercent() != null ? domain.getProgressPercent().value() : BigDecimal.ZERO;
        return new EnrollmentJpaEntity(
                domain.getId(),
                domain.getStudentId(),
                domain.getCourseId(),
                0, // completedLessons recalculated from countCompletedLessons
                0, // totalLessons
                progressBd,
                domain.isCompleted(),
                domain.getCompletedAt(),
                domain.getEnrolledAt(),
                Instant.now()
        );
    }
}
