package com.beeacademy.backend.application.assessment.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuizResult(
        UUID attemptId,
        UUID studentId,
        UUID chapterId,
        double score,
        boolean passed,
        int totalQuestions,
        int correctCount,
        Instant submittedAt,
        List<QuestionReviewItem> reviews
) {
}
