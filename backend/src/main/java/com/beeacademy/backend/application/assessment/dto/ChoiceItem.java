package com.beeacademy.backend.application.assessment.dto;

import java.util.UUID;

public record ChoiceItem(
        String content,
        String imageUrl,
        boolean isCorrect,
        int position
) {
}
