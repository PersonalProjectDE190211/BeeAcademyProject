package com.beeacademy.backend.api.common;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Cấu trúc phản hồi lỗi chuẩn cho toàn bộ REST API của BeeAcademy.
 */
public record ApiErrorResponse(
        boolean success,
        String code,
        String message,
        List<FieldErrorItem> fieldErrors,
        Instant timestamp
) {
    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(false, code, message, Collections.emptyList(), Instant.now());
    }

    public static ApiErrorResponse withFieldErrors(String code, String message, List<FieldErrorItem> fieldErrors) {
        return new ApiErrorResponse(false, code, message, fieldErrors == null ? Collections.emptyList() : fieldErrors, Instant.now());
    }
}

