package com.beeacademy.backend.application.commerce.dto;

import java.util.UUID;

public record WebhookProcessResult(
        boolean success,
        UUID orderId,
        Long orderCode,
        String orderStatus,
        String message
) {}

