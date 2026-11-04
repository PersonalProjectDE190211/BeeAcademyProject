package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Bài tập tự luận do Giáo viên giao cho bài học (UC16, UC35).
 */
public class Assignment extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID lessonId;
    private final UUID teacherId;
    private String title;
    private String description;
    private ScorePercent maxScore;
    private Instant deadline;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    public Assignment(UUID id,
                      UUID lessonId,
                      UUID teacherId,
                      String title,
                      String description,
                      ScorePercent maxScore,
                      Instant deadline,
                      boolean active,
                      Instant createdAt,
                      Instant updatedAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (lessonId == null) throw new DomainException("INVALID_LESSON_ID", "Lesson ID không được null");
        if (teacherId == null) throw new DomainException("INVALID_TEACHER_ID", "Teacher ID không được null");
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề bài tập không được để trống");

        this.id = id;
        this.lessonId = lessonId;
        this.teacherId = teacherId;
        this.title = title.trim();
        this.description = description != null ? description.trim() : "";
        this.maxScore = maxScore != null ? maxScore : ScorePercent.of(100.0);
        this.deadline = deadline;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Assignment create(UUID lessonId, UUID teacherId, String title, String description, Instant deadline) {
        Instant now = Instant.now();
        return new Assignment(UUID.randomUUID(), lessonId, teacherId, title, description, ScorePercent.of(100.0), deadline, true, now, now);
    }

    public void update(String title, String description, Instant deadline) {
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề bài tập không được để trống");
        this.title = title.trim();
        this.description = description != null ? description.trim() : "";
        this.deadline = deadline;
        this.updatedAt = Instant.now();
    }

    public boolean isExpired(Instant now) {
        return this.deadline != null && now.isAfter(this.deadline);
    }

    public UUID getId() { return id; }
    public UUID getLessonId() { return lessonId; }
    public UUID getTeacherId() { return teacherId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ScorePercent getMaxScore() { return maxScore; }
    public Instant getDeadline() { return deadline; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
