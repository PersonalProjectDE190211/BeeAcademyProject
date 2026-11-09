package com.beeacademy.backend.application.course.dto;

import com.beeacademy.backend.domain.course.CourseDocument;

import java.util.UUID;

public record CourseDocumentDto(
        UUID id,
        String title,
        String fileUrl,
        long fileSizeBytes,
        String documentType
) {
    public static CourseDocumentDto from(CourseDocument doc) {
        if (doc == null) return null;
        return new CourseDocumentDto(
                doc.getId(),
                doc.getTitle(),
                doc.getFileUrl(),
                doc.getFileSizeBytes(),
                doc.getDocumentType() != null ? doc.getDocumentType().name().toLowerCase() : "pdf"
        );
    }
}

