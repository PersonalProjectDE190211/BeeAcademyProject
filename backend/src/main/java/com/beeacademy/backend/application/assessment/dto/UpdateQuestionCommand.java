package com.beeacademy.backend.application.assessment.dto;

import java.util.List;
import java.util.UUID;

public record UpdateQuestionCommand(
        UUID questionId,
        UUID teacherId,
        String content,
        String explanation,
        String difficulty,
        List<ChoiceItem> choices,
        String updateReason
) {
}
