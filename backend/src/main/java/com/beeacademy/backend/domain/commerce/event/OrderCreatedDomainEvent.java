package com.beeacademy.backend.domain.commerce.event;

import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.common.MoneyVnd;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedDomainEvent(
        UUID eventId,
        UUID orderId,
        UUID studentId,
        MoneyVnd totalAmount,
        String paymentRef,
        Instant occurredAt
) implements DomainEvent {
    public OrderCreatedDomainEvent(UUID orderId, UUID studentId, MoneyVnd totalAmount, String paymentRef) {
        this(UUID.randomUUID(), orderId, studentId, totalAmount, paymentRef, Instant.now());
    }
}
