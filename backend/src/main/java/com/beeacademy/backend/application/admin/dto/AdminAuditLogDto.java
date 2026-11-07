package com.beeacademy.backend.application.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminAuditLogDto(
        UUID id,
        String entityType,
        UUID entityId,
        String action,
        UUID actorId,
        String actorRole,
        String reason,
        String diffJson,
        String metadataJson,
        Instant createdAt
) {
}

