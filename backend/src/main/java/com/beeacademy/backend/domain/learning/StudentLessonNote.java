package com.beeacademy.backend.domain.learning;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Ghi chú bài học của học viên tại mốc thời gian video cụ thể.
 */
public class StudentLessonNote extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID studentId;
    private final UUID courseId;
    private final UUID lessonId;
    private final int videoTimestampSec;
    private String content;
    private final Instant createdAt;
    private Instant updatedAt;

    public StudentLessonNote(UUID id,
                             UUID studentId,
                             UUID courseId,
                             UUID lessonId,
                             int videoTimestampSec,
                             String content,
                             Instant createdAt,
                             Instant updatedAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (studentId == null) throw new DomainException("INVALID_STUDENT_ID", "Student ID không được null");
        if (lessonId == null) throw new DomainException("INVALID_LESSON_ID", "Lesson ID không được null");
        if (content == null || content.isBlank()) throw new DomainException("INVALID_CONTENT", "Nội dung ghi chú không được để trống");

        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.videoTimestampSec = Math.max(0, videoTimestampSec);
        this.content = content.trim();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static StudentLessonNote create(UUID studentId, UUID courseId, UUID lessonId, int videoTimestampSec, String content) {
        Instant now = Instant.now();
        return new StudentLessonNote(UUID.randomUUID(), studentId, courseId, lessonId, videoTimestampSec, content, now, now);
    }

    public void updateContent(String newContent) {
        if (newContent == null || newContent.isBlank()) {
            throw new DomainException("INVALID_CONTENT", "Nội dung ghi chú không được để trống");
        }
        this.content = newContent.trim();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getStudentId() { return studentId; }
    public UUID getCourseId() { return courseId; }
    public UUID getLessonId() { return lessonId; }
    public int getVideoTimestampSec() { return videoTimestampSec; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

