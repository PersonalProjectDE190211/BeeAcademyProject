package com.beeacademy.backend.application.assessment.dto;

import com.beeacademy.backend.domain.assessment.QuizConfig;
import java.util.UUID;

public record QuizConfigResult(
        UUID id,
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
    public static QuizConfigResult from(QuizConfig config) {
        return new QuizConfigResult(
                config.getId(),
                config.getChapterId(),
                config.getTeacherId(),
                config.getTotalQuestions(),
                config.getEasyCount(),
                config.getMediumCount(),
                config.getHardCount(),
                config.getTimeLimitMinutes(),
                config.getPassingScore().value().doubleValue(),
                config.isShuffleQuestions(),
                config.isShuffleChoices(),
                config.getMaxAttempts()
        );
    }
}
