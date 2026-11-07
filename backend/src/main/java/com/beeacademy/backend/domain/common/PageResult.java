package com.beeacademy.backend.domain.common;

import java.util.List;

/**
 * Kết quả phân trang thuần Java ở tầng Domain (không phụ thuộc Spring Data).
 */
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {
    public int totalPages() {
        if (size <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / size);
    }
}

