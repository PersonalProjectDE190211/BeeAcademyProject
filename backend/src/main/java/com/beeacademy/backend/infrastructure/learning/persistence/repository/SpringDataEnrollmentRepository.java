package com.beeacademy.backend.infrastructure.learning.persistence.repository;

import com.beeacademy.backend.infrastructure.learning.persistence.entity.EnrollmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, UUID> {

    Optional<EnrollmentJpaEntity> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<EnrollmentJpaEntity> findByStudentIdOrderByEnrolledAtDesc(UUID studentId);

    List<EnrollmentJpaEntity> findByCourseId(UUID courseId);

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
}
