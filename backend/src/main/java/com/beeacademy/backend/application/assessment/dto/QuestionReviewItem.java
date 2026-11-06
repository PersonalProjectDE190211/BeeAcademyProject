package com.beeacademy.backend.application.assessment.dto;

import java.util.UUID;

public record QuestionReviewItem(
        UUID questionId,
        String content,
        String explanation,
        UUID selectedChoiceId,
        UUID correctChoiceId,
        boolean isCorrect
) {
}
