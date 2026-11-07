package com.beeacademy.backend.infrastructure.commerce.persistence.repository;

import com.beeacademy.backend.infrastructure.commerce.persistence.entity.TeacherBankAuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataTeacherBankAuditLogRepository extends JpaRepository<TeacherBankAuditLogJpaEntity, UUID> {

    List<TeacherBankAuditLogJpaEntity> findByTeacherIdOrderByChangedAtDesc(UUID teacherId);
}

