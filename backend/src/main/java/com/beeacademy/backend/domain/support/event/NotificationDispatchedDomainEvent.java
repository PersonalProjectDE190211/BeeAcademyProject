package com.beeacademy.backend.domain.support.event;

import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.support.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationDispatchedDomainEvent(
        UUID eventId,
        UUID notificationId,
        UUID recipientId,
        String title,
        NotificationType type,
        Instant occurredAt
) implements DomainEvent {
    public NotificationDispatchedDomainEvent(UUID notificationId, UUID recipientId, String title, NotificationType type) {
        this(UUID.randomUUID(), notificationId, recipientId, title, type, Instant.now());
    }
}
