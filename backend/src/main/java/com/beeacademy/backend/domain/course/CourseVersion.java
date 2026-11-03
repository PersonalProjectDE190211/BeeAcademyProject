package com.beeacademy.backend.domain.course;

import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity lưu snapshot nội dung của khóa học qua các lần phát hành / nâng cấp phiên bản.
 */
public class CourseVersion {

    private final UUID id;
    private final UUID courseId;
    private final int versionNo;
    private final String changelog;
    private final String snapshotJson;
    private final Instant createdAt;

    public CourseVersion(UUID id, UUID courseId, int versionNo, String changelog, String snapshotJson, Instant createdAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (courseId == null) throw new DomainException("INVALID_COURSE_ID", "Course ID không được null");
        if (versionNo < 1) throw new DomainException("INVALID_VERSION_NO", "Version number phải >= 1");

        this.id = id;
        this.courseId = courseId;
        this.versionNo = versionNo;
        this.changelog = changelog != null ? changelog.trim() : "";
        this.snapshotJson = snapshotJson != null ? snapshotJson : "{}";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static CourseVersion create(UUID courseId, int versionNo, String changelog, String snapshotJson) {
        return new CourseVersion(UUID.randomUUID(), courseId, versionNo, changelog, snapshotJson, Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getCourseId() { return courseId; }
    public int getVersionNo() { return versionNo; }
    public String getChangelog() { return changelog; }
    public String getSnapshotJson() { return snapshotJson; }
    public Instant getCreatedAt() { return createdAt; }
}
