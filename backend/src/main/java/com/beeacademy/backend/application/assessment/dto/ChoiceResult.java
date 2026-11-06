package com.beeacademy.backend.application.assessment.dto;

import com.beeacademy.backend.domain.assessment.QuestionChoice;
import java.util.UUID;

public record ChoiceResult(
        UUID id,
        String content,
        String imageUrl,
        boolean isCorrect,
        int position
) {
    public static ChoiceResult from(QuestionChoice choice) {
        return new ChoiceResult(
                choice.getId(),
                choice.getContent(),
                choice.getImageUrl(),
                choice.isCorrect(),
                choice.getPosition()
        );
    }
}
