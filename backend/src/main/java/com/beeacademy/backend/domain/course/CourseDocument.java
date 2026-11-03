package com.beeacademy.backend.domain.course;

import com.beeacademy.backend.domain.common.DomainException;

import java.util.UUID;

/**
 * Entity tài liệu đính kèm của một bài học.
 */
public class CourseDocument {

    private final UUID id;
    private final UUID lessonId;
    private String title;
    private String fileUrl;
    private long fileSizeBytes;
    private DocumentType documentType;

    public CourseDocument(UUID id, UUID lessonId, String title, String fileUrl, long fileSizeBytes, DocumentType documentType) {
        if (id == null) throw new DomainException("INVALID_ID", "Document ID không được null");
        if (lessonId == null) throw new DomainException("INVALID_LESSON_ID", "Lesson ID không được null");
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề tài liệu không được trống");
        if (fileUrl == null || fileUrl.isBlank()) throw new DomainException("INVALID_FILE_URL", "Đường dẫn tài liệu không được trống");

        this.id = id;
        this.lessonId = lessonId;
        this.title = title.trim();
        this.fileUrl = fileUrl.trim();
        this.fileSizeBytes = Math.max(0, fileSizeBytes);
        this.documentType = documentType != null ? documentType : DocumentType.OTHER;
    }

    public static CourseDocument create(UUID lessonId, String title, String fileUrl, long fileSizeBytes, DocumentType documentType) {
        return new CourseDocument(UUID.randomUUID(), lessonId, title, fileUrl, fileSizeBytes, documentType);
    }

    public UUID getId() { return id; }
    public UUID getLessonId() { return lessonId; }
    public String getTitle() { return title; }
    public String getFileUrl() { return fileUrl; }
    public long getFileSizeBytes() { return fileSizeBytes; }
    public DocumentType getDocumentType() { return documentType; }
}

