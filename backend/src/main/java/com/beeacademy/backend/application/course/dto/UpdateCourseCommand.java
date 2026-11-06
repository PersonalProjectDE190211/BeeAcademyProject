package com.beeacademy.backend.application.course.dto;

import java.util.List;
import java.util.UUID;

public record UpdateCourseCommand(
        UUID courseId,
        UUID teacherId,
        UUID categoryId,
        String title,
        String description,
        String thumbnailUrl,
        int priceVnd,
        List<Integer> grades
) {
}

