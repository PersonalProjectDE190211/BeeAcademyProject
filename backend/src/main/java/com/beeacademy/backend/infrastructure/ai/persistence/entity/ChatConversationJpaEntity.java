package com.beeacademy.backend.infrastructure.ai.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_conversations")
public class ChatConversationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "current_intent", length = 50)
    private String currentIntent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "current_filters", columnDefinition = "jsonb", nullable = false)
    private String currentFiltersJson = "{}";

    @Column(name = "last_search_course_ids", columnDefinition = "uuid[]")
    private UUID[] lastSearchCourseIds = new UUID[0];

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ChatConversationJpaEntity() {
    }

    public ChatConversationJpaEntity(UUID id, UUID userId, String currentIntent, String currentFiltersJson, UUID[] lastSearchCourseIds, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.userId = userId;
        this.currentIntent = currentIntent;
        this.currentFiltersJson = currentFiltersJson != null ? currentFiltersJson : "{}";
        this.lastSearchCourseIds = lastSearchCourseIds != null ? lastSearchCourseIds : new UUID[0];
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getCurrentIntent() {
        return currentIntent;
    }

    public void setCurrentIntent(String currentIntent) {
        this.currentIntent = currentIntent;
    }

    public String getCurrentFiltersJson() {
        return currentFiltersJson;
    }

    public void setCurrentFiltersJson(String currentFiltersJson) {
        this.currentFiltersJson = currentFiltersJson != null ? currentFiltersJson : "{}";
    }

    public UUID[] getLastSearchCourseIds() {
        return lastSearchCourseIds != null ? lastSearchCourseIds : new UUID[0];
    }

    public void setLastSearchCourseIds(UUID[] lastSearchCourseIds) {
        this.lastSearchCourseIds = lastSearchCourseIds != null ? lastSearchCourseIds : new UUID[0];
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

