package com.beeacademy.backend.application.assessment.dto;

import java.util.UUID;

public record SaveQuizConfigCommand(
        UUID chapterId,
        UUID teacherId,
        int totalQuestions,
        int easyCount,
        int mediumCount,
        int hardCount,
        Integer timeLimitMinutes,
        double passingScore,
        boolean shuffleQuestions,
        boolean shuffleChoices,
        Integer maxAttempts
) {
}
