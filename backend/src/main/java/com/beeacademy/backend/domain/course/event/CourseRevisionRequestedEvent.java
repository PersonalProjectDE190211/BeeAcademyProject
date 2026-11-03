package com.beeacademy.backend.domain.course.event;

import com.beeacademy.backend.domain.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Sự kiện: Khóa học được Quản trị viên (Admin) yêu cầu chỉnh sửa (NEEDS_REVISION).
 */
public record CourseRevisionRequestedEvent(
        UUID eventId,
        UUID courseId,
        UUID teacherId,
        UUID adminId,
        String courseTitle,
        String feedback,
        Instant occurredAt
) implements DomainEvent {

    public static CourseRevisionRequestedEvent of(UUID courseId, UUID teacherId, UUID adminId, String title, String feedback) {
        return new CourseRevisionRequestedEvent(
                UUID.randomUUID(),
                courseId,
                teacherId,
                adminId,
                title,
                feedback,
                Instant.now()
        );
    }
}
