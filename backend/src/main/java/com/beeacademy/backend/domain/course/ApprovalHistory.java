package com.beeacademy.backend.domain.course;

import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity lưu vết lịch sử phê duyệt, từ chối hoặc yêu cầu sửa đổi của Quản trị viên (Admin).
 */
public class ApprovalHistory {

    private final UUID id;
    private final UUID courseId;
    private final UUID reviewerId;
    private final CourseStatus action;
    private final String comment;
    private final Instant createdAt;

    public ApprovalHistory(UUID id, UUID courseId, UUID reviewerId, CourseStatus action, String comment, Instant createdAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (courseId == null) throw new DomainException("INVALID_COURSE_ID", "Course ID không được null");
        if (reviewerId == null) throw new DomainException("INVALID_REVIEWER_ID", "Reviewer ID không được null");
        if (action == null) throw new DomainException("INVALID_ACTION", "Action không được null");

        this.id = id;
        this.courseId = courseId;
        this.reviewerId = reviewerId;
        this.action = action;
        this.comment = comment != null ? comment.trim() : "";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static ApprovalHistory record(UUID courseId, UUID reviewerId, CourseStatus action, String comment) {
        return new ApprovalHistory(UUID.randomUUID(), courseId, reviewerId, action, comment, Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getCourseId() { return courseId; }
    public UUID getReviewerId() { return reviewerId; }
    public CourseStatus getAction() { return action; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
