package com.beeacademy.backend.domain.course;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.CourseSlug;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.course.event.CourseApprovedEvent;
import com.beeacademy.backend.domain.course.event.CourseRejectedEvent;
import com.beeacademy.backend.domain.course.event.CourseRevisionRequestedEvent;
import com.beeacademy.backend.domain.course.event.CourseSubmittedForReviewEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Khóa học (Course).
 * Quản lý cấu trúc cây Chương - Bài học, State Machine quy trình kiểm duyệt (DRAFT -> PENDING_REVIEW -> PUBLISHED / REJECTED / NEEDS_REVISION),
 * và tự động bảo đảm tính toàn vẹn của các chỉ số tổng hợp (totalChapters, totalLessons, totalDurationSec).
 */
public class Course extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID teacherId;
    private UUID categoryId;
    private String title;
    private CourseSlug slug;
    private String description;
    private String thumbnailUrl;
    private MoneyVnd price;
    private CourseStatus status;
    private List<Integer> grades;
    private int totalChapters;
    private int totalLessons;
    private int totalDurationSec;
    private BigDecimal averageRating;
    private int totalReviews;
    private Instant publishedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private final List<Chapter> chapters;
    private final List<ApprovalHistory> approvalHistories;

    public Course(UUID id,
                  UUID teacherId,
                  UUID categoryId,
                  String title,
                  CourseSlug slug,
                  String description,
                  String thumbnailUrl,
                  MoneyVnd price,
                  CourseStatus status,
                  List<Integer> grades,
                  int totalChapters,
                  int totalLessons,
                  int totalDurationSec,
                  BigDecimal averageRating,
                  int totalReviews,
                  Instant publishedAt,
                  Instant createdAt,
                  Instant updatedAt,
                  List<Chapter> chapters,
                  List<ApprovalHistory> approvalHistories) {
        if (id == null) throw new DomainException("INVALID_ID", "Course ID không được null");
        if (teacherId == null) throw new DomainException("INVALID_TEACHER_ID", "Teacher ID không được null");
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề khóa học không được để trống");

        this.id = id;
        this.teacherId = teacherId;
        this.categoryId = categoryId;
        this.title = title.trim();
        this.slug = slug != null ? slug : CourseSlug.fromTitle(this.title);
        this.description = description != null ? description.trim() : "";
        this.thumbnailUrl = thumbnailUrl != null ? thumbnailUrl.trim() : "";
        this.price = price != null ? price : MoneyVnd.ZERO;
        this.status = status != null ? status : CourseStatus.DRAFT;
        this.grades = grades != null ? new ArrayList<>(grades) : new ArrayList<>();
        this.totalChapters = totalChapters;
        this.totalLessons = totalLessons;
        this.totalDurationSec = totalDurationSec;
        this.averageRating = averageRating != null ? averageRating : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        this.totalReviews = totalReviews;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.chapters = chapters != null ? new ArrayList<>(chapters) : new ArrayList<>();
        this.approvalHistories = approvalHistories != null ? new ArrayList<>(approvalHistories) : new ArrayList<>();
    }

    /**
     * Khởi tạo một khóa học mới ở trạng thái DRAFT.
     */
    public static Course create(UUID teacherId,
                                UUID categoryId,
                                String title,
                                String description,
                                String thumbnailUrl,
                                MoneyVnd price,
                                List<Integer> grades) {
        Instant now = Instant.now();
        return new Course(
                UUID.randomUUID(),
                teacherId,
                categoryId,
                title,
                CourseSlug.fromTitle(title),
                description,
                thumbnailUrl,
                price,
                CourseStatus.DRAFT,
                grades,
                0,
                0,
                0,
                BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP),
                0,
                null,
                now,
                now,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    // ==========================================
    // Quản lý cấu trúc Chương & Bài học (Invariants)
    // ==========================================

    public Chapter addChapter(String title, String description) {
        Chapter chapter = Chapter.create(this.id, title, description, this.chapters.size());
        this.chapters.add(chapter);
        recalculateInvariants();
        this.updatedAt = Instant.now();
        return chapter;
    }

    public void removeChapter(UUID chapterId) {
        this.chapters.removeIf(c -> c.getId().equals(chapterId));
        recalculateInvariants();
        this.updatedAt = Instant.now();
    }

    public Lesson addLesson(UUID chapterId, String title, String videoUrl, int durationSec, boolean isFreePreview) {
        Chapter chapter = findChapter(chapterId)
                .orElseThrow(() -> new DomainException("CHAPTER_NOT_FOUND", "Không tìm thấy chương học: " + chapterId));

        Lesson lesson = Lesson.create(chapterId, title, chapter.getLessons().size(), videoUrl, durationSec, isFreePreview);
        chapter.addLesson(lesson);
        recalculateInvariants();
        this.updatedAt = Instant.now();
        return lesson;
    }

    public void removeLesson(UUID chapterId, UUID lessonId) {
        Chapter chapter = findChapter(chapterId)
                .orElseThrow(() -> new DomainException("CHAPTER_NOT_FOUND", "Không tìm thấy chương học: " + chapterId));

        chapter.removeLesson(lessonId);
        recalculateInvariants();
        this.updatedAt = Instant.now();
    }

    public Optional<Chapter> findChapter(UUID chapterId) {
        return this.chapters.stream()
                .filter(c -> c.getId().equals(chapterId))
                .findFirst();
    }

    private void recalculateInvariants() {
        this.totalChapters = this.chapters.size();
        this.totalLessons = this.chapters.stream()
                .mapToInt(c -> c.getLessons().size())
                .sum();
        this.totalDurationSec = this.chapters.stream()
                .mapToInt(Chapter::calculateTotalDurationSec)
                .sum();
    }

    // ==========================================
    // State Machine kiểm duyệt khóa học (UC39)
    // ==========================================

    /**
     * Giáo viên nộp khóa học để chờ Admin kiểm duyệt.
     */
    public void submitForReview() {
        if (this.status != CourseStatus.DRAFT &&
                this.status != CourseStatus.NEEDS_REVISION &&
                this.status != CourseStatus.REJECTED) {
            throw new DomainException("INVALID_STATUS_TRANSITION",
                    "Chỉ có thể nộp duyệt khóa học từ trạng thái Nháp (DRAFT), Cần sửa (NEEDS_REVISION) hoặc Bị từ chối (REJECTED). Trạng thái hiện tại: " + this.status);
        }
        if (this.totalLessons < 1) {
            throw new DomainException("COURSE_HAS_NO_LESSONS",
                    "Khóa học phải có ít nhất 1 bài học trước khi nộp kiểm duyệt");
        }

        this.status = CourseStatus.PENDING_REVIEW;
        this.updatedAt = Instant.now();
        registerDomainEvent(CourseSubmittedForReviewEvent.of(this.id, this.teacherId, this.title, this.totalLessons));
    }

    /**
     * Admin phê duyệt khóa học (PUBLISHED).
     */
    public void approve(UUID adminId, String comment) {
        if (this.status != CourseStatus.PENDING_REVIEW) {
            throw new DomainException("INVALID_STATUS_TRANSITION",
                    "Chỉ có thể phê duyệt khóa học đang ở trạng thái Chờ duyệt (PENDING_REVIEW). Trạng thái hiện tại: " + this.status);
        }

        Instant now = Instant.now();
        this.status = CourseStatus.PUBLISHED;
        this.publishedAt = now;
        this.updatedAt = now;
        this.approvalHistories.add(ApprovalHistory.record(this.id, adminId, CourseStatus.PUBLISHED, comment));
        registerDomainEvent(CourseApprovedEvent.of(this.id, this.teacherId, adminId, this.title, now));
    }

    /**
     * Admin từ chối khóa học (REJECTED).
     */
    public void reject(UUID adminId, String reason) {
        if (this.status != CourseStatus.PENDING_REVIEW) {
            throw new DomainException("INVALID_STATUS_TRANSITION",
                    "Chỉ có thể từ chối khóa học đang ở trạng thái Chờ duyệt (PENDING_REVIEW). Trạng thái hiện tại: " + this.status);
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainException("REASON_REQUIRED", "Cần nhập lý do từ chối khóa học");
        }

        Instant now = Instant.now();
        this.status = CourseStatus.REJECTED;
        this.updatedAt = now;
        this.approvalHistories.add(ApprovalHistory.record(this.id, adminId, CourseStatus.REJECTED, reason));
        registerDomainEvent(CourseRejectedEvent.of(this.id, this.teacherId, adminId, this.title, reason));
    }

    /**
     * Admin yêu cầu giáo viên sửa đổi bổ sung (NEEDS_REVISION).
     */
    public void requestRevision(UUID adminId, String feedback) {
        if (this.status != CourseStatus.PENDING_REVIEW) {
            throw new DomainException("INVALID_STATUS_TRANSITION",
                    "Chỉ có thể yêu cầu chỉnh sửa khóa học đang ở trạng thái Chờ duyệt (PENDING_REVIEW). Trạng thái hiện tại: " + this.status);
        }
        if (feedback == null || feedback.isBlank()) {
            throw new DomainException("FEEDBACK_REQUIRED", "Cần nhập nội dung yêu cầu sửa đổi cho giáo viên");
        }

        Instant now = Instant.now();
        this.status = CourseStatus.NEEDS_REVISION;
        this.updatedAt = now;
        this.approvalHistories.add(ApprovalHistory.record(this.id, adminId, CourseStatus.NEEDS_REVISION, feedback));
        registerDomainEvent(CourseRevisionRequestedEvent.of(this.id, this.teacherId, adminId, this.title, feedback));
    }

    /**
     * Lưu trữ khóa học.
     */
    public void archive() {
        if (this.status == CourseStatus.ARCHIVED) {
            throw new DomainException("ALREADY_ARCHIVED", "Khóa học đã ở trạng thái lưu trữ");
        }
        this.status = CourseStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    // ==========================================
    // Cập nhật thông tin cơ bản & giá bán
    // ==========================================

    public void updateBasicInfo(String title, String description, String thumbnailUrl, UUID categoryId, List<Integer> grades) {
        if (title == null || title.isBlank()) throw new DomainException("INVALID_TITLE", "Tiêu đề không được để trống");

        this.title = title.trim();
        this.slug = CourseSlug.fromTitle(this.title);
        this.description = description != null ? description.trim() : "";
        this.thumbnailUrl = thumbnailUrl != null ? thumbnailUrl.trim() : "";
        this.categoryId = categoryId;
        this.grades = grades != null ? new ArrayList<>(grades) : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public void updatePrice(MoneyVnd price) {
        if (price == null) throw new DomainException("INVALID_PRICE", "Giá khóa học không được null");
        this.price = price;
        this.updatedAt = Instant.now();
    }

    public void updateReviewStats(BigDecimal averageRating, int totalReviews) {
        this.averageRating = averageRating != null ? averageRating.setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        this.totalReviews = Math.max(0, totalReviews);
        this.updatedAt = Instant.now();
    }

    // ==========================================
    // Getters
    // ==========================================

    public UUID getId() { return id; }
    public UUID getTeacherId() { return teacherId; }
    public UUID getCategoryId() { return categoryId; }
    public String getTitle() { return title; }
    public CourseSlug getSlug() { return slug; }
    public String getDescription() { return description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public MoneyVnd getPrice() { return price; }
    public CourseStatus getStatus() { return status; }
    public List<Integer> getGrades() { return Collections.unmodifiableList(grades); }
    public int getTotalChapters() { return totalChapters; }
    public int getTotalLessons() { return totalLessons; }
    public int getTotalDurationSec() { return totalDurationSec; }
    public BigDecimal getAverageRating() { return averageRating; }
    public int getTotalReviews() { return totalReviews; }
    public Instant getPublishedAt() { return publishedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Chapter> getChapters() { return Collections.unmodifiableList(chapters); }
    public List<ApprovalHistory> getApprovalHistories() { return Collections.unmodifiableList(approvalHistories); }
}

