package com.beeacademy.backend.application.assessment.dto;

import java.util.UUID;

public record ExamChoiceItem(
        UUID choiceId,
        String content,
        String imageUrl,
        int position
) {
}
