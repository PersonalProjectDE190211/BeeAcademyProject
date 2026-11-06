package com.beeacademy.backend.application.assessment.dto;

import java.util.List;
import java.util.UUID;

public record ExamQuestionItem(
        UUID questionId,
        String content,
        String type,
        List<ExamChoiceItem> choices
) {
}
