package com.beeacademy.backend.infrastructure.family.persistence.adapter;

import com.beeacademy.backend.domain.family.ParentProgressAccessAudit;
import com.beeacademy.backend.domain.family.ParentProgressAccessAuditRepositoryPort;
import com.beeacademy.backend.infrastructure.family.persistence.entity.ParentProgressAccessAuditJpaEntity;
import com.beeacademy.backend.infrastructure.family.persistence.repository.SpringDataParentProgressAccessAuditRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ParentProgressAccessAuditRepositoryAdapter implements ParentProgressAccessAuditRepositoryPort {

    private final SpringDataParentProgressAccessAuditRepository auditRepository;

    public ParentProgressAccessAuditRepositoryAdapter(SpringDataParentProgressAccessAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    @Transactional
    public ParentProgressAccessAudit save(ParentProgressAccessAudit audit) {
        ParentProgressAccessAuditJpaEntity entity = new ParentProgressAccessAuditJpaEntity(
                audit.getId(),
                audit.getParentId(),
                audit.getStudentId(),
                audit.getAccessType(),
                audit.getAccessedAt()
        );

        ParentProgressAccessAuditJpaEntity saved = auditRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentProgressAccessAudit> findByStudentId(UUID studentId) {
        return auditRepository.findByStudentIdOrderByAccessedAtDesc(studentId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentProgressAccessAudit> findByParentId(UUID parentId) {
        return auditRepository.findByParentIdOrderByAccessedAtDesc(parentId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ParentProgressAccessAudit toDomain(ParentProgressAccessAuditJpaEntity entity) {
        return new ParentProgressAccessAudit(
                entity.getId(),
                entity.getParentId(),
                entity.getStudentId(),
                entity.getAccessType(),
                entity.getAccessedAt()
        );
    }
}

