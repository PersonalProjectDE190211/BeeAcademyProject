package com.beeacademy.backend.application.commerce.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateOrderResult(
        UUID orderId,
        Long orderCode,
        String paymentRef,
        String checkoutUrl,
        String qrCodeUrl,
        int totalAmountVnd,
        int discountAmountVnd,
        String status,
        Instant expiresAt,
        List<OrderItemResult> items
) {}

