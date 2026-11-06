package com.beeacademy.backend.domain.common;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepositoryPort {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findByEntity(AuditEntityType entityType, UUID entityId);

    List<AuditLog> findByActorId(UUID actorId);
}
