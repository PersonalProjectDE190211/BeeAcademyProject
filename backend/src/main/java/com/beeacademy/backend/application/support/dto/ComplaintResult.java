package com.beeacademy.backend.application.support.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ComplaintResult(
        UUID id,
        UUID senderId,
        String senderRole,
        String title,
        String category,
        String priority,
        String status,
        Instant createdAt,
        Instant lastActivityAt,
        Instant resolvedAt,
        UUID resolvedByAdminId,
        String resolutionSummary,
        List<ComplaintMessageResult> messages
) {}

