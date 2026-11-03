package com.beeacademy.backend.domain.learning;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;
import com.beeacademy.backend.domain.learning.event.CourseCompletedEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho việc Ghi danh / Đăng ký khóa học của học viên.
 * Quản lý tiến độ tổng thể của toàn bộ khóa học (0.0% -> 100.0%) và phát sinh
 * sự kiện CourseCompletedEvent khi học viên học xong tất cả các bài học.
 */
public class Enrollment extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID studentId;
    private final UUID courseId;
    private final UUID orderId;
    private ScorePercent progressPercent;
    private boolean completed;
    private Instant completedAt;
    private Instant lastAccessedAt;
    private final Instant enrolledAt;

    public Enrollment(UUID id,
                      UUID studentId,
                      UUID courseId,
                      UUID orderId,
                      ScorePercent progressPercent,
                      boolean completed,
                      Instant completedAt,
                      Instant lastAccessedAt,
                      Instant enrolledAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (studentId == null) throw new DomainException("INVALID_STUDENT_ID", "Student ID không được null");
        if (courseId == null) throw new DomainException("INVALID_COURSE_ID", "Course ID không được null");

        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.orderId = orderId;
        this.progressPercent = progressPercent != null ? progressPercent : ScorePercent.ZERO;
        this.completed = completed;
        this.completedAt = completedAt;
        this.enrolledAt = enrolledAt != null ? enrolledAt : Instant.now();
        this.lastAccessedAt = lastAccessedAt != null ? lastAccessedAt : this.enrolledAt;
    }

    public static Enrollment create(UUID studentId, UUID courseId, UUID orderId) {
        Instant now = Instant.now();
        return new Enrollment(
                UUID.randomUUID(),
                studentId,
                courseId,
                orderId,
                ScorePercent.ZERO,
                false,
                null,
                now,
                now
        );
    }

    /**
     * Cập nhật tiến độ học tập dựa trên số bài học đã hoàn thành.
     *
     * @param completedLessonsCount Số bài học đã hoàn thành (xem >= 80%)
     * @param totalLessonsCount     Tổng số bài học trong khóa học
     * @param now                   Thời điểm cập nhật
     */
    public void updateProgress(int completedLessonsCount, int totalLessonsCount, Instant now) {
        if (completedLessonsCount < 0) {
            throw new DomainException("INVALID_COMPLETED_COUNT", "Số bài học hoàn thành không được âm");
        }
        if (totalLessonsCount <= 0) {
            return;
        }

        Instant updateTime = now != null ? now : Instant.now();
        this.lastAccessedAt = updateTime;

        int cappedCompleted = Math.min(completedLessonsCount, totalLessonsCount);
        this.progressPercent = ScorePercent.fromRatio(cappedCompleted, totalLessonsCount);

        // Đạt 100% bài học -> đánh dấu hoàn thành khóa học
        if (cappedCompleted == totalLessonsCount && !this.completed) {
            this.completed = true;
            this.completedAt = updateTime;
            registerDomainEvent(CourseCompletedEvent.of(this.studentId, this.courseId));
        }
    }

    public UUID getId() { return id; }
    public UUID getStudentId() { return studentId; }
    public UUID getCourseId() { return courseId; }
    public UUID getOrderId() { return orderId; }
    public ScorePercent getProgressPercent() { return progressPercent; }
    public boolean isCompleted() { return completed; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public Instant getEnrolledAt() { return enrolledAt; }
}

