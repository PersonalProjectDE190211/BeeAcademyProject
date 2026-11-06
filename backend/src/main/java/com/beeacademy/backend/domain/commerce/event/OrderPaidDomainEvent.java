package com.beeacademy.backend.domain.commerce.event;

import com.beeacademy.backend.domain.commerce.OrderItem;
import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.common.MoneyVnd;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPaidDomainEvent(
        UUID eventId,
        UUID orderId,
        UUID studentId,
        MoneyVnd totalAmount,
        String paymentRef,
        Long orderCode,
        List<OrderItem> items,
        Instant occurredAt
) implements DomainEvent {
    public OrderPaidDomainEvent(
            UUID orderId,
            UUID studentId,
            MoneyVnd totalAmount,
            String paymentRef,
            Long orderCode,
            List<OrderItem> items
    ) {
        this(UUID.randomUUID(), orderId, studentId, totalAmount, paymentRef, orderCode, List.copyOf(items), Instant.now());
    }
}

