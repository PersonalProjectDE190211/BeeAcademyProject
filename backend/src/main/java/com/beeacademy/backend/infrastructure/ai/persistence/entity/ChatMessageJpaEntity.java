package com.beeacademy.backend.infrastructure.ai.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ChatMessageJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "role", nullable = false, length = 20)
    private String role; // "user" or "model"

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "intent", length = 50)
    private String intent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "courses_snapshot", columnDefinition = "jsonb", nullable = false)
    private String coursesSnapshotJson = "[]";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ChatMessageJpaEntity() {
    }

    public ChatMessageJpaEntity(UUID id, UUID conversationId, String role, String content, String intent, String coursesSnapshotJson, Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.intent = intent;
        this.coursesSnapshotJson = coursesSnapshotJson != null ? coursesSnapshotJson : "[]";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getCoursesSnapshotJson() {
        return coursesSnapshotJson;
    }

    public void setCoursesSnapshotJson(String coursesSnapshotJson) {
        this.coursesSnapshotJson = coursesSnapshotJson != null ? coursesSnapshotJson : "[]";
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

