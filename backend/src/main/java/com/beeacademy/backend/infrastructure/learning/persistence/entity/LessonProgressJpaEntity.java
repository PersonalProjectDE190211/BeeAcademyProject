package com.beeacademy.backend.infrastructure.learning.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity ánh xạ bảng 'lesson_progress' (Tiến độ bài học và phân đoạn xem video).
 */
@Entity
@Table(name = "lesson_progress")
public class LessonProgressJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "watched_seconds", nullable = false)
    private int watchedSeconds;

    @Column(name = "last_position_sec", nullable = false)
    private int lastPositionSec;

    @Column(name = "watch_segments", columnDefinition = "JSONB", nullable = false)
    private String watchSegments;

    @Column(name = "notes", columnDefinition = "JSONB", nullable = false)
    private String notes;

    @Column(name = "downloaded_doc_ids", columnDefinition = "JSONB", nullable = false)
    private String downloadedDocIds;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LessonProgressJpaEntity() {
    }

    public LessonProgressJpaEntity(UUID id,
                                   UUID studentId,
                                   UUID lessonId,
                                   UUID courseId,
                                   int watchedSeconds,
                                   int lastPositionSec,
                                   String watchSegments,
                                   String notes,
                                   String downloadedDocIds,
                                   boolean isCompleted,
                                   Instant completedAt,
                                   Instant updatedAt) {
        this.id = id;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.watchedSeconds = watchedSeconds;
        this.lastPositionSec = lastPositionSec;
        this.watchSegments = watchSegments != null ? watchSegments : "[]";
        this.notes = notes != null ? notes : "[]";
        this.downloadedDocIds = downloadedDocIds != null ? downloadedDocIds : "[]";
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }
    public UUID getLessonId() { return lessonId; }
    public void setLessonId(UUID lessonId) { this.lessonId = lessonId; }
    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }
    public int getWatchedSeconds() { return watchedSeconds; }
    public void setWatchedSeconds(int watchedSeconds) { this.watchedSeconds = watchedSeconds; }
    public int getLastPositionSec() { return lastPositionSec; }
    public void setLastPositionSec(int lastPositionSec) { this.lastPositionSec = lastPositionSec; }
    public String getWatchSegments() { return watchSegments; }
    public void setWatchSegments(String watchSegments) { this.watchSegments = watchSegments; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getDownloadedDocIds() { return downloadedDocIds; }
    public void setDownloadedDocIds(String downloadedDocIds) { this.downloadedDocIds = downloadedDocIds; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean isCompleted) { this.isCompleted = isCompleted; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
