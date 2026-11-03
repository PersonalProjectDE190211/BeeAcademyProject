package com.beeacademy.backend.domain.learning;

import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity tin nhắn phản hồi trong luồng Hỏi & Đáp (Q&A) bài học.
 */
public class QaMessage {

    private final UUID id;
    private final UUID threadId;
    private final UUID senderId;
    private final String content;
    private final Instant createdAt;

    public QaMessage(UUID id, UUID threadId, UUID senderId, String content, Instant createdAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (threadId == null) throw new DomainException("INVALID_THREAD_ID", "Thread ID không được null");
        if (senderId == null) throw new DomainException("INVALID_SENDER_ID", "Sender ID không được null");
        if (content == null || content.isBlank()) throw new DomainException("INVALID_CONTENT", "Nội dung phản hồi không được để trống");

        this.id = id;
        this.threadId = threadId;
        this.senderId = senderId;
        this.content = content.trim();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static QaMessage create(UUID threadId, UUID senderId, String content) {
        return new QaMessage(UUID.randomUUID(), threadId, senderId, content, Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getThreadId() { return threadId; }
    public UUID getSenderId() { return senderId; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
}

