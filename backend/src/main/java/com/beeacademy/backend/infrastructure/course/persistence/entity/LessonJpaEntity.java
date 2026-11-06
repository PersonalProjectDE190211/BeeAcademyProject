package com.beeacademy.backend.infrastructure.course.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity ánh xạ với bảng 'lessons' (Bài học / Bài giảng video).
 */
@Entity
@Table(name = "lessons")
public class LessonJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private ChapterJpaEntity chapter;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "is_free_preview", nullable = false)
    private boolean freePreview;

    @Column(name = "video_storage_path", length = 1000)
    private String videoStoragePath;

    @Column(name = "duration_sec", nullable = false)
    private int durationSec;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "documents", columnDefinition = "jsonb")
    private String documentsJson = "[]";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LessonJpaEntity() {
    }

    public LessonJpaEntity(UUID id,
                           ChapterJpaEntity chapter,
                           String title,
                           String description,
                           int position,
                           boolean freePreview,
                           String videoStoragePath,
                           int durationSec,
                           String documentsJson,
                           Instant createdAt,
                           Instant updatedAt) {
        this.id = id;
        this.chapter = chapter;
        this.title = title;
        this.description = description;
        this.position = position;
        this.freePreview = freePreview;
        this.videoStoragePath = videoStoragePath;
        this.durationSec = durationSec;
        this.documentsJson = documentsJson != null ? documentsJson : "[]";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ChapterJpaEntity getChapter() { return chapter; }
    public void setChapter(ChapterJpaEntity chapter) { this.chapter = chapter; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public boolean isFreePreview() { return freePreview; }
    public void setFreePreview(boolean freePreview) { this.freePreview = freePreview; }

    public String getVideoStoragePath() { return videoStoragePath; }
    public void setVideoStoragePath(String videoStoragePath) { this.videoStoragePath = videoStoragePath; }

    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }

    public String getDocumentsJson() { return documentsJson; }
    public void setDocumentsJson(String documentsJson) { this.documentsJson = documentsJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

