package com.beeacademy.backend.domain.learning;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho một luồng thảo luận / hỏi đáp (Q&A) trong bài học.
 */
public class QaThread extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID courseId;
    private final UUID lessonId;
    private final UUID studentId;
    private String title;
    private String content;
    private boolean resolved;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<QaMessage> messages;

    public QaThread(UUID id,
                    UUID courseId,
                    UUID lessonId,
                    UUID studentId,
                    String title,
                    String content,
                    boolean resolved,
                    Instant createdAt,
                    Instant updatedAt,
                    List<QaMessage> messages) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (courseId == null) throw new DomainException("INVALID_COURSE_ID", "Course ID không được null");
        if (lessonId == null) throw new DomainException("INVALID_LESSON_ID", "Lesson ID không được null");
        if (studentId == null) throw new DomainException("INVALID_STUDENT_ID", "Student ID không được null");
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề câu hỏi không được để trống");
        if (content == null || content.isBlank()) throw new DomainException("INVALID_CONTENT", "Nội dung câu hỏi không được để trống");

        this.id = id;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.studentId = studentId;
        this.title = title.trim();
        this.content = content.trim();
        this.resolved = resolved;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
    }

    public static QaThread open(UUID studentId, UUID courseId, UUID lessonId, String title, String content) {
        Instant now = Instant.now();
        return new QaThread(
                UUID.randomUUID(),
                courseId,
                lessonId,
                studentId,
                title,
                content,
                false,
                now,
                now,
                new ArrayList<>()
        );
    }

    public QaMessage addReply(UUID senderId, String replyContent) {
        QaMessage message = QaMessage.create(this.id, senderId, replyContent);
        this.messages.add(message);
        this.updatedAt = Instant.now();
        return message;
    }

    public void markResolved(boolean resolved) {
        this.resolved = resolved;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCourseId() { return courseId; }
    public UUID getLessonId() { return lessonId; }
    public UUID getStudentId() { return studentId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isResolved() { return resolved; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<QaMessage> getMessages() { return Collections.unmodifiableList(messages); }
}

