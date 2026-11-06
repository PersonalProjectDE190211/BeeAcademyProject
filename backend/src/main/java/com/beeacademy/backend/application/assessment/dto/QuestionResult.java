package com.beeacademy.backend.application.assessment.dto;

import com.beeacademy.backend.domain.assessment.Question;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record QuestionResult(
        UUID id,
        UUID teacherId,
        UUID categoryId,
        UUID chapterId,
        int grade,
        String content,
        String explanation,
        String difficulty,
        String type,
        String status,
        int usageCount,
        Instant createdAt,
        Instant updatedAt,
        List<ChoiceResult> choices
) {
    public static QuestionResult from(Question q) {
        List<ChoiceResult> choiceResults = q.getChoices() != null
                ? q.getChoices().stream().map(ChoiceResult::from).toList()
                : Collections.emptyList();

        return new QuestionResult(
                q.getId(),
                q.getTeacherId(),
                q.getCategoryId(),
                q.getChapterId(),
                q.getGrade(),
                q.getContent(),
                q.getExplanation(),
                q.getDifficulty().name(),
                q.getType().name(),
                q.getStatus().name(),
                q.getUsageCount(),
                q.getCreatedAt(),
                q.getUpdatedAt(),
                choiceResults
        );
    }
}
