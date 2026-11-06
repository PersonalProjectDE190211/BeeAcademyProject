package com.beeacademy.backend.application.assessment.dto;

import java.util.List;
import java.util.UUID;

public record CreateQuestionCommand(
        UUID teacherId,
        UUID categoryId,
        UUID chapterId,
        int grade,
        String content,
        String explanation,
        String difficulty,
        String type,
        List<ChoiceItem> choices,
        List<String> tags
) {
}
