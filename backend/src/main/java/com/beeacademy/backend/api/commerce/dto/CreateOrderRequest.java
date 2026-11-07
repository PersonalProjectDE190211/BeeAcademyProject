package com.beeacademy.backend.api.commerce.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotEmpty(message = "Danh sách khóa học không được để trống")
        List<UUID> courseIds,
        String voucherCode,
        String paymentMethod
) {}

