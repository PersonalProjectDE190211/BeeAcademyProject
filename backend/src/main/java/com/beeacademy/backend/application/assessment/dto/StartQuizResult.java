package com.beeacademy.backend.application.assessment.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StartQuizResult(
        UUID attemptId,
        UUID chapterId,
        int totalQuestions,
        Integer timeLimitMinutes,
        double passingScore,
        Instant startedAt,
        List<ExamQuestionItem> questions
) {
}
