package com.beeacademy.backend.domain.family.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ParentLinkRevokedDomainEvent(
        UUID eventId,
        UUID linkId,
        UUID parentId,
        UUID studentId,
        UUID revokedBy,
        String reason,
        Instant occurredAt
) implements DomainEvent {
    public ParentLinkRevokedDomainEvent(UUID linkId, UUID parentId, UUID studentId, UUID revokedBy, String reason) {
        this(UUID.randomUUID(), linkId, parentId, studentId, revokedBy, reason, Instant.now());
    }
}
