package com.beeacademy.backend.api.ai.dto;

import java.util.UUID;

/**
 * DTO dữ liệu có cấu trúc của Khóa học trả về cho BeeBot Frontend render Course Card.
 */
public record ChatbotCourseCardDto(
        UUID id,
        String title,
        String subject,
        String subjectLabel,
        Integer grade,
        int price,
        Integer originalPrice,
        double rating,
        int reviewCount,
        int totalLessons,
        String thumbnailUrl,
        String detailUrl
) {
}

