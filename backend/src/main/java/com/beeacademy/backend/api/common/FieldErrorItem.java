package com.beeacademy.backend.api.common;

/**
 * Chi tiết lỗi xác thực trên từng trường (Field Validation Error).
 */
public record FieldErrorItem(
        String field,
        String message,
        Object rejectedValue
) {}

