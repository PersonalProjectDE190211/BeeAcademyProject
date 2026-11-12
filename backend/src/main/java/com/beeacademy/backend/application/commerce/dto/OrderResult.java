package com.beeacademy.backend.application.commerce.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResult(
        UUID id,
        UUID studentId,
        Long orderCode,
        String paymentRef,
        String paymentMethod,
        String status,
        int totalAmountVnd,
        int discountAmountVnd,
        Instant expiresAt,
        Instant paidAt,
        Instant createdAt,
        Instant updatedAt,
        List<OrderItemResult> items,
        String checkoutUrl,
        String qrCodeUrl,
        String bankBin,
        String bankName,
        String accountNumber,
        String accountName
) {
    public OrderResult(
            UUID id,
            UUID studentId,
            Long orderCode,
            String paymentRef,
            String paymentMethod,
            String status,
            int totalAmountVnd,
            int discountAmountVnd,
            Instant expiresAt,
            Instant paidAt,
            Instant createdAt,
            Instant updatedAt,
            List<OrderItemResult> items
    ) {
        this(id, studentId, orderCode, paymentRef, paymentMethod, status, totalAmountVnd, discountAmountVnd, expiresAt, paidAt, createdAt, updatedAt, items, null, null, null, null, null, null);
    }
}

