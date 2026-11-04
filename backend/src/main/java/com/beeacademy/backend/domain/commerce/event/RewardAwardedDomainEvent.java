package com.beeacademy.backend.domain.commerce.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record RewardAwardedDomainEvent(
        UUID eventId,
        UUID studentId,
        int pointsAwarded,
        int newTotalPoints,
        String reason,
        Instant occurredAt
) implements DomainEvent {
    public RewardAwardedDomainEvent(UUID studentId, int pointsAwarded, int newTotalPoints, String reason) {
        this(UUID.randomUUID(), studentId, pointsAwarded, newTotalPoints, reason, Instant.now());
    }
}
