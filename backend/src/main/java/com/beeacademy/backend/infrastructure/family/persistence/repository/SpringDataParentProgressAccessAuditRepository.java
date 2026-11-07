package com.beeacademy.backend.infrastructure.family.persistence.repository;

import com.beeacademy.backend.infrastructure.family.persistence.entity.ParentProgressAccessAuditJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataParentProgressAccessAuditRepository extends JpaRepository<ParentProgressAccessAuditJpaEntity, UUID> {

    List<ParentProgressAccessAuditJpaEntity> findByStudentIdOrderByAccessedAtDesc(UUID studentId);

    List<ParentProgressAccessAuditJpaEntity> findByParentIdOrderByAccessedAtDesc(UUID parentId);
}

