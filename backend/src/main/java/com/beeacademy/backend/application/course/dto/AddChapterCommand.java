package com.beeacademy.backend.application.course.dto;

import java.util.UUID;

public record AddChapterCommand(
        UUID courseId,
        UUID teacherId,
        String title,
        String description
) {
}

