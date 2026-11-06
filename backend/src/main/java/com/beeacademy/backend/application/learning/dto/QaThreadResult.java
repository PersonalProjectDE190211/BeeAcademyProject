package com.beeacademy.backend.application.learning.dto;

import com.beeacademy.backend.domain.learning.QaThread;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QaThreadResult(
        UUID id,
        UUID courseId,
        UUID lessonId,
        UUID studentId,
        String title,
        String content,
        boolean isResolved,
        Instant createdAt,
        Instant updatedAt,
        List<QaMessageResult> messages
) {
    public static QaThreadResult from(QaThread thread) {
        List<QaMessageResult> msgDtos = thread.getMessages() != null
                ? thread.getMessages().stream().map(QaMessageResult::from).toList()
                : List.of();

        return new QaThreadResult(
                thread.getId(),
                thread.getCourseId(),
                thread.getLessonId(),
                thread.getStudentId(),
                thread.getTitle(),
                thread.getContent(),
                thread.isResolved(),
                thread.getCreatedAt(),
                thread.getUpdatedAt(),
                msgDtos
        );
    }
}
