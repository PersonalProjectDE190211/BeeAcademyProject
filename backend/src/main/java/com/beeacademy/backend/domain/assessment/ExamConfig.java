package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Đề thi kiểm tra chính thức trong khóa học (UC34).
 * Cố định nội dung đề thi bằng snapshot JSON tại thời điểm tạo/kích hoạt (questionsSnapshot)
 * để đảm bảo tính bất biến, không bị ảnh hưởng nếu ngân hàng câu hỏi bị sửa sau này.
 */
public class ExamConfig extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID courseId;
    private final int placementChapterIndex;
    private final UUID teacherId;
    private String title;
    private String description;
    private int timeLimitMinutes;
    private ScorePercent passingScore;
    private int maxAttempts;
    private String questionsSnapshot;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    public ExamConfig(UUID id,
                      UUID courseId,
                      int placementChapterIndex,
                      UUID teacherId,
                      String title,
                      String description,
                      int timeLimitMinutes,
                      ScorePercent passingScore,
                      int maxAttempts,
                      String questionsSnapshot,
                      boolean active,
                      Instant createdAt,
                      Instant updatedAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (courseId == null) throw new DomainException("INVALID_COURSE_ID", "Course ID không được null");
        if (teacherId == null) throw new DomainException("INVALID_TEACHER_ID", "Teacher ID không được null");
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề bài thi không được để trống");
        if (timeLimitMinutes <= 0) throw new DomainException("INVALID_TIME_LIMIT", "Thời gian làm bài phải > 0 phút");
        if (maxAttempts <= 0) throw new DomainException("INVALID_MAX_ATTEMPTS", "Số lần thi tối đa phải >= 1");

        this.id = id;
        this.courseId = courseId;
        this.placementChapterIndex = placementChapterIndex;
        this.teacherId = teacherId;
        this.title = title.trim();
        this.description = description != null ? description.trim() : "";
        this.timeLimitMinutes = timeLimitMinutes;
        this.passingScore = passingScore != null ? passingScore : ScorePercent.of(60.0);
        this.maxAttempts = maxAttempts;
        this.questionsSnapshot = questionsSnapshot != null ? questionsSnapshot : "[]";
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static ExamConfig create(UUID courseId,
                                    int placementChapterIndex,
                                    UUID teacherId,
                                    String title,
                                    String description,
                                    int timeLimitMinutes,
                                    ScorePercent passingScore,
                                    int maxAttempts,
                                    String questionsSnapshot) {
        Instant now = Instant.now();
        return new ExamConfig(
                UUID.randomUUID(),
                courseId,
                placementChapterIndex,
                teacherId,
                title,
                description,
                timeLimitMinutes,
                passingScore,
                maxAttempts,
                questionsSnapshot,
                true,
                now,
                now
        );
    }

    public void update(String title,
                       String description,
                       int timeLimitMinutes,
                       ScorePercent passingScore,
                       int maxAttempts,
                       String questionsSnapshot) {
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề bài thi không được để trống");
        if (timeLimitMinutes <= 0) throw new DomainException("INVALID_TIME_LIMIT", "Thời gian làm bài phải > 0 phút");
        if (maxAttempts <= 0) throw new DomainException("INVALID_MAX_ATTEMPTS", "Số lần thi tối đa phải >= 1");

        this.title = title.trim();
        this.description = description != null ? description.trim() : "";
        this.timeLimitMinutes = timeLimitMinutes;
        this.passingScore = passingScore != null ? passingScore : this.passingScore;
        this.maxAttempts = maxAttempts;
        if (questionsSnapshot != null && !questionsSnapshot.isBlank()) {
            this.questionsSnapshot = questionsSnapshot;
        }
        this.updatedAt = Instant.now();
    }

    public void deactivate() { this.active = false; this.updatedAt = Instant.now(); }
    public void activate() { this.active = true; this.updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getCourseId() { return courseId; }
    public int getPlacementChapterIndex() { return placementChapterIndex; }
    public UUID getTeacherId() { return teacherId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getTimeLimitMinutes() { return timeLimitMinutes; }
    public ScorePercent getPassingScore() { return passingScore; }
    public int getMaxAttempts() { return maxAttempts; }
    public String getQuestionsSnapshot() { return questionsSnapshot; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
