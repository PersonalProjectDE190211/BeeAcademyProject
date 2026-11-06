package com.beeacademy.backend.infrastructure.learning.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity ánh xạ bảng 'lesson_discussions' (Hỏi đáp Q&A dưới bài giảng).
 */
@Entity
@Table(name = "lesson_discussions")
public class LessonDiscussionJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "author_role", nullable = false, length = 20)
    private String authorRole;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_resolved", nullable = false)
    private boolean isResolved;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public LessonDiscussionJpaEntity() {
    }

    public LessonDiscussionJpaEntity(UUID id,
                                    UUID lessonId,
                                    UUID courseId,
                                    UUID authorId,
                                    String authorRole,
                                    UUID parentId,
                                    String content,
                                    boolean isResolved,
                                    Instant createdAt,
                                    Instant updatedAt) {
        this.id = id;
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.authorId = authorId;
        this.authorRole = authorRole;
        this.parentId = parentId;
        this.content = content;
        this.isResolved = isResolved;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getLessonId() { return lessonId; }
    public void setLessonId(UUID lessonId) { this.lessonId = lessonId; }
    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
    public String getAuthorRole() { return authorRole; }
    public void setAuthorRole(String authorRole) { this.authorRole = authorRole; }
    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isResolved() { return isResolved; }
    public void setResolved(boolean isResolved) { this.isResolved = isResolved; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
