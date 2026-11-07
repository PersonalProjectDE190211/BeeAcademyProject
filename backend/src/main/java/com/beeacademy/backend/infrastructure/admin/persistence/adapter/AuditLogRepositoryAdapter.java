package com.beeacademy.backend.infrastructure.admin.persistence.adapter;

import com.beeacademy.backend.domain.common.AuditEntityType;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.domain.common.AuditLogRepositoryPort;
import com.beeacademy.backend.infrastructure.admin.persistence.entity.AuditLogJpaEntity;
import com.beeacademy.backend.infrastructure.admin.persistence.repository.SpringDataAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final SpringDataAuditLogRepository repository;

    public AuditLogRepositoryAdapter(SpringDataAuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogJpaEntity entity = toEntity(auditLog);
        AuditLogJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<AuditLog> findByEntity(AuditEntityType entityType, UUID entityId) {
        return repository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType.name(), entityId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> findByActorId(UUID actorId) {
        return repository.findByActorIdOrderByCreatedAtDesc(actorId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public Page<AuditLog> findAll(Pageable pageable) {
        return repository.findAllByOrderByCreatedAtDesc(pageable).map(this::toDomain);
    }

    public Page<AuditLog> findByEntityType(AuditEntityType entityType, Pageable pageable) {
        return repository.findByEntityTypeOrderByCreatedAtDesc(entityType.name(), pageable).map(this::toDomain);
    }

    private AuditLogJpaEntity toEntity(AuditLog domain) {
        return new AuditLogJpaEntity(
                domain.getId(),
                domain.getEntityType().name(),
                domain.getEntityId(),
                domain.getAction(),
                domain.getActorId(),
                domain.getActorRole(),
                domain.getReason(),
                domain.getDiffJson(),
                domain.getMetadataJson(),
                domain.getCreatedAt()
        );
    }

    private AuditLog toDomain(AuditLogJpaEntity entity) {
        AuditEntityType type;
        try {
            type = AuditEntityType.valueOf(entity.getEntityType().toUpperCase());
        } catch (IllegalArgumentException e) {
            type = AuditEntityType.SYSTEM_SETTING;
        }

        return new AuditLog(
                entity.getId(),
                type,
                entity.getEntityId(),
                entity.getAction(),
                entity.getActorId(),
                entity.getActorRole(),
                entity.getReason(),
                entity.getDiffJson(),
                entity.getMetadataJson(),
                entity.getCreatedAt()
        );
    }
}

