package com.beeacademy.backend.infrastructure.admin.persistence.repository;

import com.beeacademy.backend.infrastructure.admin.persistence.entity.AuditLogJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogJpaEntity, UUID> {

    List<AuditLogJpaEntity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);

    List<AuditLogJpaEntity> findByActorIdOrderByCreatedAtDesc(UUID actorId);

    Page<AuditLogJpaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLogJpaEntity> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);
}

