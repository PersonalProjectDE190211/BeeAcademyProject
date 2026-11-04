package com.beeacademy.backend.domain.support;

import com.beeacademy.backend.domain.common.DomainException;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity đính kèm minh chứng cho khiếu nại (hình ảnh chụp màn hình chuyển khoản, lỗi bài học...).
 */
public class ComplaintAttachment implements Serializable {

    private final UUID id;
    private final UUID complaintId;
    private final String fileUrl;
    private final String fileName;
    private final long fileSizeBytes;
    private final Instant uploadedAt;

    public ComplaintAttachment(
            UUID id,
            UUID complaintId,
            String fileUrl,
            String fileName,
            long fileSizeBytes,
            Instant uploadedAt
    ) {
        if (id == null) {
            throw new DomainException("INVALID_ATTACHMENT", "ID đính kèm không được null");
        }
        if (complaintId == null) {
            throw new DomainException("INVALID_ATTACHMENT", "Complaint ID không được null");
        }
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new DomainException("INVALID_ATTACHMENT", "URL file đính kèm không được rỗng");
        }
        this.id = id;
        this.complaintId = complaintId;
        this.fileUrl = fileUrl.trim();
        this.fileName = fileName == null ? "attachment" : fileName.trim();
        this.fileSizeBytes = Math.max(0, fileSizeBytes);
        this.uploadedAt = uploadedAt != null ? uploadedAt : Instant.now();
    }

    public static ComplaintAttachment create(UUID complaintId, String fileUrl, String fileName, long fileSizeBytes) {
        return new ComplaintAttachment(UUID.randomUUID(), complaintId, fileUrl, fileName, fileSizeBytes, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getComplaintId() {
        return complaintId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplaintAttachment that = (ComplaintAttachment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
