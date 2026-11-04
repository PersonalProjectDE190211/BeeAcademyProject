package com.beeacademy.backend.domain.family.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ParentLinkAcceptedDomainEvent(
        UUID eventId,
        UUID linkId,
        UUID parentId,
        UUID studentId,
        Instant linkedAt,
        Instant occurredAt
) implements DomainEvent {
    public ParentLinkAcceptedDomainEvent(UUID linkId, UUID parentId, UUID studentId, Instant linkedAt) {
        this(UUID.randomUUID(), linkId, parentId, studentId, linkedAt, Instant.now());
    }
}
