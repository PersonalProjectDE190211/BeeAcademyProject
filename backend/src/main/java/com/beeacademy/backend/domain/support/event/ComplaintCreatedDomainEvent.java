package com.beeacademy.backend.domain.support.event;

import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.support.ComplaintCategory;
import com.beeacademy.backend.domain.support.ComplaintPriority;

import java.time.Instant;
import java.util.UUID;

public record ComplaintCreatedDomainEvent(
        UUID eventId,
        UUID complaintId,
        UUID senderId,
        String senderRole,
        String title,
        ComplaintCategory category,
        ComplaintPriority priority,
        Instant occurredAt
) implements DomainEvent {
    public ComplaintCreatedDomainEvent(
            UUID complaintId,
            UUID senderId,
            String senderRole,
            String title,
            ComplaintCategory category,
            ComplaintPriority priority
    ) {
        this(UUID.randomUUID(), complaintId, senderId, senderRole, title, category, priority, Instant.now());
    }
}
