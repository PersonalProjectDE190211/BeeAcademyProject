package com.beeacademy.backend.application.commerce.dto;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
        UUID studentId,
        List<UUID> courseIds,
        String voucherCode,
        String paymentMethod
) {
    public CreateOrderCommand {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID không được để trống");
        }
        if (courseIds == null || courseIds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách khóa học không được để trống");
        }
    }
}

