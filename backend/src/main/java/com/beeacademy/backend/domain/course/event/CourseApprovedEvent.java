package com.beeacademy.backend.domain.course.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Khóa học đã được Quản trị viên (Admin) phê duyệt xuất bản (PUBLISHED).
 */
public record CourseApprovedEvent(
        UUID eventId,
        UUID courseId,
        UUID teacherId,
        UUID approvedByAdminId,
        String courseTitle,
        Instant publishedAt,
        Instant occurredAt
) implements DomainEvent {

    public static CourseApprovedEvent of(UUID courseId, UUID teacherId, UUID adminId, String title, Instant publishedAt) {
        return new CourseApprovedEvent(
                UUID.randomUUID(),
                courseId,
                teacherId,
                adminId,
                title,
                publishedAt,
                Instant.now()
        );
    }
}
