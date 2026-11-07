package com.beeacademy.backend.application.commerce.dto;

import java.util.UUID;

public record OrderItemResult(
        UUID id,
        UUID courseId,
        UUID teacherId,
        String courseTitle,
        int priceAtPurchase,
        int platformFeeRate,
        int platformFeeVnd,
        int teacherAmountVnd,
        String itemStatus
) {}

