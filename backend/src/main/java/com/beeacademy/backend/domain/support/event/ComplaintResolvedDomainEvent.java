package com.beeacademy.backend.domain.support.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ComplaintResolvedDomainEvent(
        UUID eventId,
        UUID complaintId,
        UUID senderId,
        UUID resolvedByAdminId,
        String resolutionSummary,
        Instant resolvedAt,
        Instant occurredAt
) implements DomainEvent {
    public ComplaintResolvedDomainEvent(
            UUID complaintId,
            UUID senderId,
            UUID resolvedByAdminId,
            String resolutionSummary,
            Instant resolvedAt
    ) {
        this(UUID.randomUUID(), complaintId, senderId, resolvedByAdminId, resolutionSummary, resolvedAt, Instant.now());
    }
}
