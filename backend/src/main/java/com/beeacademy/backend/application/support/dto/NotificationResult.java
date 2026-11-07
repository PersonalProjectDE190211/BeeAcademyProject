package com.beeacademy.backend.application.support.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResult(
        UUID id,
        UUID recipientId,
        String title,
        String message,
        String type,
        String actionUrl,
        boolean read,
        Instant createdAt,
        Instant readAt
) {}

