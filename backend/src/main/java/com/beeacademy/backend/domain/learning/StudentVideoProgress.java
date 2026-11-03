package com.beeacademy.backend.domain.learning;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.learning.event.LessonCompletedEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root theo dõi tiến độ xem video bài học của học viên (UC14).
 * Kiểm soát chống gian lận thời gian xem bằng cấu trúc WatchSegments,
 * tự động kích hoạt sự kiện LessonCompletedEvent khi người học xem >= 80% thời lượng.
 */
public class StudentVideoProgress extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID studentId;
    private final UUID courseId;
    private final UUID lessonId;
    private final WatchSegments watchSegments;
    private int lastPositionSec;
    private boolean completed;
    private Instant completedAt;
    private Instant updatedAt;

    public StudentVideoProgress(UUID id,
                                UUID studentId,
                                UUID courseId,
                                UUID lessonId,
                                WatchSegments watchSegments,
                                int lastPositionSec,
                                boolean completed,
                                Instant completedAt,
                                Instant updatedAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (studentId == null) throw new DomainException("INVALID_STUDENT_ID", "Student ID không được null");
        if (courseId == null) throw new DomainException("INVALID_COURSE_ID", "Course ID không được null");
        if (lessonId == null) throw new DomainException("INVALID_LESSON_ID", "Lesson ID không được null");

        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.watchSegments = watchSegments != null ? watchSegments : WatchSegments.empty();
        this.lastPositionSec = Math.max(0, lastPositionSec);
        this.completed = completed;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static StudentVideoProgress create(UUID studentId, UUID courseId, UUID lessonId) {
        return new StudentVideoProgress(
                UUID.randomUUID(),
                studentId,
                courseId,
                lessonId,
                WatchSegments.empty(),
                0,
                false,
                null,
                Instant.now()
        );
    }

    /**
     * Ghi nhận một phiên xem video mới (ví dụ ping định kỳ 5 giây/lần từ video player).
     *
     * @param startSec               Giây bắt đầu khoảng vừa xem
     * @param endSec                 Giây kết thúc khoảng vừa xem
     * @param currentPositionSec     Vị trí playback hiện tại của player
     * @param totalLessonDurationSec Tổng thời lượng của video bài học (giây)
     * @param now                    Thời điểm ghi nhận
     */
    public void recordWatch(int startSec, int endSec, int currentPositionSec, int totalLessonDurationSec, Instant now) {
        this.watchSegments.addSegment(startSec, endSec);
        this.lastPositionSec = Math.max(0, currentPositionSec);
        this.updatedAt = now != null ? now : Instant.now();

        // Kiểm tra ngưỡng hoàn thành >= 80% (UC14)
        if (!this.completed && this.watchSegments.hasReachedCompletionThreshold(totalLessonDurationSec, 0.8)) {
            this.completed = true;
            this.completedAt = this.updatedAt;
            registerDomainEvent(LessonCompletedEvent.of(this.studentId, this.courseId, this.lessonId));
        }
    }

    public UUID getId() { return id; }
    public UUID getStudentId() { return studentId; }
    public UUID getCourseId() { return courseId; }
    public UUID getLessonId() { return lessonId; }
    public WatchSegments getWatchSegments() { return watchSegments; }
    public int getLastPositionSec() { return lastPositionSec; }
    public boolean isCompleted() { return completed; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

