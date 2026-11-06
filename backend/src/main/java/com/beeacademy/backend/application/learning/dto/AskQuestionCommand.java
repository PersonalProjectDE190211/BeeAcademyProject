package com.beeacademy.backend.application.learning.dto;

import java.util.UUID;

public record AskQuestionCommand(
        UUID courseId,
        UUID lessonId,
        UUID studentId,
        String title,
        String content
) {
}
