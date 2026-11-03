package com.beeacademy.backend.domain.course.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Khóa học bị Quản trị viên (Admin) từ chối phê duyệt (REJECTED).
 */
public record CourseRejectedEvent(
        UUID eventId,
        UUID courseId,
        UUID teacherId,
        UUID rejectedByAdminId,
        String courseTitle,
        String reason,
        Instant occurredAt
) implements DomainEvent {

    public static CourseRejectedEvent of(UUID courseId, UUID teacherId, UUID adminId, String title, String reason) {
        return new CourseRejectedEvent(
                UUID.randomUUID(),
                courseId,
                teacherId,
                adminId,
                title,
                reason,
                Instant.now()
        );
    }
}

