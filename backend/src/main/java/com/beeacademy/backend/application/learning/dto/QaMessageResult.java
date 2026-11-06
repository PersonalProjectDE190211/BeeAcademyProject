package com.beeacademy.backend.application.learning.dto;

import com.beeacademy.backend.domain.learning.QaMessage;

import java.time.Instant;
import java.util.UUID;

public record QaMessageResult(
        UUID id,
        UUID threadId,
        UUID senderId,
        String content,
        Instant createdAt
) {
    public static QaMessageResult from(QaMessage message) {
        return new QaMessageResult(
                message.getId(),
                message.getThreadId(),
                message.getSenderId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
