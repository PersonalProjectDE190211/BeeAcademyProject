package com.beeacademy.backend.domain.family.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ParentLinkInvitedDomainEvent(
        UUID eventId,
        UUID linkId,
        UUID parentId,
        String studentEmail,
        String inviteToken,
        Instant expiresAt,
        Instant occurredAt
) implements DomainEvent {
    public ParentLinkInvitedDomainEvent(
            UUID linkId,
            UUID parentId,
            String studentEmail,
            String inviteToken,
            Instant expiresAt
    ) {
        this(UUID.randomUUID(), linkId, parentId, studentEmail, inviteToken, expiresAt, Instant.now());
    }
}
