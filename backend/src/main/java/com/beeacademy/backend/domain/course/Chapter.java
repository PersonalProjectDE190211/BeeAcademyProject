package com.beeacademy.backend.domain.course;

import com.beeacademy.backend.domain.common.DomainException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Entity đại diện cho một chương học (Chapter) thuộc Khóa học.
 */
public class Chapter {

    private final UUID id;
    private final UUID courseId;
    private String title;
    private String description;
    private int orderIndex;
    private final List<Lesson> lessons;

    public Chapter(UUID id,
                   UUID courseId,
                   String title,
                   String description,
                   int orderIndex,
                   List<Lesson> lessons) {
        if (id == null) throw new DomainException("INVALID_ID", "Chapter ID không được null");
        if (courseId == null) throw new DomainException("INVALID_COURSE_ID", "Course ID không được null");
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề chương không được để trống");

        this.id = id;
        this.courseId = courseId;
        this.title = title.trim();
        this.description = description != null ? description.trim() : "";
        this.orderIndex = Math.max(0, orderIndex);
        this.lessons = lessons != null ? new ArrayList<>(lessons) : new ArrayList<>();
    }

    public static Chapter create(UUID courseId, String title, String description, int orderIndex) {
        return new Chapter(UUID.randomUUID(), courseId, title, description, orderIndex, new ArrayList<>());
    }

    public void update(String title, String description) {
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề chương không được để trống");
        this.title = title.trim();
        this.description = description != null ? description.trim() : "";
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = Math.max(0, orderIndex);
    }

    public void addLesson(Lesson lesson) {
        if (lesson == null) throw new DomainException("INVALID_LESSON", "Bài học không được null");
        this.lessons.add(lesson);
    }

    public void removeLesson(UUID lessonId) {
        this.lessons.removeIf(l -> l.getId().equals(lessonId));
    }

    public Optional<Lesson> findLesson(UUID lessonId) {
        return this.lessons.stream()
                .filter(l -> l.getId().equals(lessonId))
                .findFirst();
    }

    public int calculateTotalDurationSec() {
        return this.lessons.stream()
                .mapToInt(Lesson::getDurationSec)
                .sum();
    }

    public UUID getId() { return id; }
    public UUID getCourseId() { return courseId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getOrderIndex() { return orderIndex; }
    public List<Lesson> getLessons() { return Collections.unmodifiableList(lessons); }
}

