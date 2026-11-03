package com.beeacademy.backend.domain.course;

import com.beeacademy.backend.domain.common.DomainException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entity đại diện cho một bài học trong một chương (Chapter).
 * Hỗ trợ đánh dấu học thử miễn phí (isFreePreview = true - UC08).
 */
public class Lesson {

    private final UUID id;
    private final UUID chapterId;
    private String title;
    private int orderIndex;
    private String videoUrl;
    private int durationSec;
    private boolean isFreePreview;
    private final List<CourseDocument> documents;

    public Lesson(UUID id,
                  UUID chapterId,
                  String title,
                  int orderIndex,
                  String videoUrl,
                  int durationSec,
                  boolean isFreePreview,
                  List<CourseDocument> documents) {
        if (id == null) throw new DomainException("INVALID_ID", "Lesson ID không được null");
        if (chapterId == null) throw new DomainException("INVALID_CHAPTER_ID", "Chapter ID không được null");
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề bài học không được để trống");
        if (durationSec < 0) throw new DomainException("INVALID_DURATION", "Thời lượng bài học không được âm");

        this.id = id;
        this.chapterId = chapterId;
        this.title = title.trim();
        this.orderIndex = Math.max(0, orderIndex);
        this.videoUrl = videoUrl != null ? videoUrl.trim() : "";
        this.durationSec = durationSec;
        this.isFreePreview = isFreePreview;
        this.documents = documents != null ? new ArrayList<>(documents) : new ArrayList<>();
    }

    public static Lesson create(UUID chapterId, String title, int orderIndex, String videoUrl, int durationSec, boolean isFreePreview) {
        return new Lesson(UUID.randomUUID(), chapterId, title, orderIndex, videoUrl, durationSec, isFreePreview, new ArrayList<>());
    }

    public void update(String title, String videoUrl, int durationSec, boolean isFreePreview) {
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề bài học không được để trống");
        if (durationSec < 0) throw new DomainException("INVALID_DURATION", "Thời lượng bài học không được âm");

        this.title = title.trim();
        this.videoUrl = videoUrl != null ? videoUrl.trim() : "";
        this.durationSec = durationSec;
        this.isFreePreview = isFreePreview;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = Math.max(0, orderIndex);
    }

    public void addDocument(CourseDocument document) {
        if (document == null) throw new DomainException("INVALID_DOCUMENT", "Tài liệu không được null");
        this.documents.add(document);
    }

    public void removeDocument(UUID documentId) {
        this.documents.removeIf(doc -> doc.getId().equals(documentId));
    }

    public UUID getId() { return id; }
    public UUID getChapterId() { return chapterId; }
    public String getTitle() { return title; }
    public int getOrderIndex() { return orderIndex; }
    public String getVideoUrl() { return videoUrl; }
    public int getDurationSec() { return durationSec; }
    public boolean isFreePreview() { return isFreePreview; }
    public List<CourseDocument> getDocuments() { return Collections.unmodifiableList(documents); }
}
