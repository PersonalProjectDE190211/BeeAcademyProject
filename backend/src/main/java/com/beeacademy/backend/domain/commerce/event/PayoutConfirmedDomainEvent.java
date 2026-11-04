package com.beeacademy.backend.domain.commerce.event;

import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.common.MoneyVnd;

import java.time.Instant;
import java.util.UUID;

public record PayoutConfirmedDomainEvent(
        UUID eventId,
        UUID payoutPeriodId,
        UUID teacherId,
        String monthYear,
        MoneyVnd teacherAmount,
        String transferRef,
        UUID confirmedByAdminId,
        Instant occurredAt
) implements DomainEvent {
    public PayoutConfirmedDomainEvent(
            UUID payoutPeriodId,
            UUID teacherId,
            String monthYear,
            MoneyVnd teacherAmount,
            String transferRef,
            UUID confirmedByAdminId
    ) {
        this(UUID.randomUUID(), payoutPeriodId, teacherId, monthYear, teacherAmount, transferRef, confirmedByAdminId, Instant.now());
    }
}
