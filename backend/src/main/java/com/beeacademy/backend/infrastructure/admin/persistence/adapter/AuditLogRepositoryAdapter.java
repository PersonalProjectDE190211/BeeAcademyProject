package com.beeacademy.backend.infrastructure.admin.persistence.adapter;

import com.beeacademy.backend.domain.common.AuditEntityType;
import com.beeacademy.backend.domain.common.AuditLog;
import com.beeacademy.backend.domain.common.AuditLogRepositoryPort;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.PageResult;
import com.beeacademy.backend.infrastructure.admin.persistence.entity.AuditLogJpaEntity;
import com.beeacademy.backend.infrastructure.admin.persistence.repository.SpringDataAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

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
                .toList();
    }

    @Override
    public List<AuditLog> findByActorId(UUID actorId) {
        return repository.findByActorIdOrderByCreatedAtDesc(actorId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PageResult<AuditLog> findPaged(AuditEntityType entityType, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<AuditLogJpaEntity> entityPage = entityType != null
                ? repository.findByEntityTypeOrderByCreatedAtDesc(entityType.name(), pageable)
                : repository.findAllByOrderByCreatedAtDesc(pageable);

        List<AuditLog> content = entityPage.getContent().stream()
                .map(this::toDomain)
                .toList();
        return new PageResult<>(content, pageable.getPageNumber(), pageable.getPageSize(), entityPage.getTotalElements());
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
            type = AuditEntityType.valueOf(entity.getEntityType().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new DomainException("INVALID_AUDIT_ENTITY_TYPE",
                    "Loại thực thể kiểm toán không hợp lệ trong cơ sở dữ liệu: " + entity.getEntityType());
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
