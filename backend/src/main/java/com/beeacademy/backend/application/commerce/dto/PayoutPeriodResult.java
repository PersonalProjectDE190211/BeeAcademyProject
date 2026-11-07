package com.beeacademy.backend.application.commerce.dto;

import java.time.Instant;
import java.util.UUID;

public record PayoutPeriodResult(
        UUID id,
        UUID teacherId,
        String monthYear,
        String status,
        int totalGrossVnd,
        int totalPlatformFeeVnd,
        int totalTeacherVnd,
        int transactionCount,
        Instant paidAt,
        UUID paidByAdmin,
        String transferRef,
        String transferContent,
        String uncAttachmentUrl,
        Instant createdAt,
        Instant updatedAt
) {}

