package com.beeacademy.backend.application.commerce.dto;

public record TeacherRevenueStatsResult(
        int totalGrossVnd,
        int totalPlatformFeeVnd,
        int totalTeacherVnd,
        int pendingPayoutVnd,
        int paidPayoutVnd,
        int totalTransactions,
        String currentPeriod
) {}

