package com.beeacademy.backend.infrastructure.course.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Entity ánh xạ với bảng 'chapters' (Chương học).
 */
@Entity
@Table(name = "chapters")
public class ChapterJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseJpaEntity course;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @org.hibernate.annotations.BatchSize(size = 50)
    private List<LessonJpaEntity> lessons = new ArrayList<>();

    public ChapterJpaEntity() {
    }

    public ChapterJpaEntity(UUID id,
                            CourseJpaEntity course,
                            String title,
                            String description,
                            int position,
                            Instant createdAt,
                            Instant updatedAt) {
        this.id = id;
        this.course = course;
        this.title = title;
        this.description = description;
        this.position = position;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CourseJpaEntity getCourse() { return course; }
    public void setCourse(CourseJpaEntity course) { this.course = course; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<LessonJpaEntity> getLessons() { return lessons; }
    public void setLessons(List<LessonJpaEntity> lessons) { this.lessons = lessons; }
}

