package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.assessment.event.ExamRetakeApprovedEvent;
import com.beeacademy.backend.domain.assessment.event.ExamRetakeRequestedEvent;
import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Đơn xin mở lại lượt thi kiểm tra (UC17-19).
 * Quy tắc nghiệp vụ bắt buộc:
 * 1. Học sinh chỉ được xin thi lại tối đa 5 lần cho một đề thi (MAX_RETAKE_LIMIT = 5).
 * 2. Nếu Giáo viên từ chối, học sinh phải chờ thời gian hồi (Cooldown) 12 giờ trước khi được gửi lại đơn mới.
 * 3. Khi được Giáo viên duyệt, lượt thi mở thêm có thời hạn (grantExpiresAt). Quá hạn sẽ chuyển sang trạng thái EXPIRED.
 */
public class ExamRetakeRequest extends AbstractAggregateRoot {

    public static final int MAX_RETAKE_LIMIT = 5;
    public static final Duration REJECTION_COOLDOWN = Duration.ofHours(12);

    private final UUID id;
    private final UUID examConfigId;
    private final UUID studentId;
    private final int requestCount;
    private String reason;
    private RetakeRequestStatus status;
    private Instant rejectionCooldownUntil;
    private Instant grantExpiresAt;
    private UUID reviewedByTeacherId;
    private String reviewComment;
    private final Instant createdAt;
    private Instant reviewedAt;

    public ExamRetakeRequest(UUID id,
                             UUID examConfigId,
                             UUID studentId,
                             int requestCount,
                             String reason,
                             RetakeRequestStatus status,
                             Instant rejectionCooldownUntil,
                             Instant grantExpiresAt,
                             UUID reviewedByTeacherId,
                             String reviewComment,
                             Instant createdAt,
                             Instant reviewedAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (examConfigId == null) throw new DomainException("INVALID_CONFIG_ID", "ExamConfig ID không được null");
        if (studentId == null) throw new DomainException("INVALID_STUDENT_ID", "Student ID không được null");
        if (requestCount < 1) throw new DomainException("INVALID_REQUEST_COUNT", "Số lần yêu cầu phải >= 1");
        if (reason == null || reason.isBlank()) throw new DomainException("INVALID_REASON", "Lý do xin thi lại không được để trống");

        this.id = id;
        this.examConfigId = examConfigId;
        this.studentId = studentId;
        this.requestCount = requestCount;
        this.reason = reason.trim();
        this.status = status != null ? status : RetakeRequestStatus.PENDING;
        this.rejectionCooldownUntil = rejectionCooldownUntil;
        this.grantExpiresAt = grantExpiresAt;
        this.reviewedByTeacherId = reviewedByTeacherId;
        this.reviewComment = reviewComment != null ? reviewComment.trim() : "";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.reviewedAt = reviewedAt;
    }

    /**
     * Học sinh tạo yêu cầu xin thi lại mới.
     *
     * @param studentId                    ID học sinh
     * @param examConfigId                 ID đề thi
     * @param reason                       Lý do xin thi lại
     * @param priorRequestsCount           Tổng số lần đã từng gửi yêu cầu trước đó
     * @param lastRejectionCooldownUntil   Thời điểm kết thúc cooldown của lần bị từ chối gần nhất (nếu có)
     * @param now                          Thời điểm hiện tại
     */
    public static ExamRetakeRequest createRequest(UUID studentId,
                                                  UUID examConfigId,
                                                  String reason,
                                                  int priorRequestsCount,
                                                  Instant lastRejectionCooldownUntil,
                                                  Instant now) {
        Instant current = now != null ? now : Instant.now();

        // 1. Kiểm tra giới hạn 5 lần
        if (priorRequestsCount >= MAX_RETAKE_LIMIT) {
            throw new DomainException("MAX_RETAKE_EXCEEDED",
                    "Bạn đã đạt giới hạn tối đa " + MAX_RETAKE_LIMIT + " lần xin thi lại cho bài thi này");
        }

        // 2. Kiểm tra Cooldown 12 giờ nếu lần trước bị từ chối
        if (lastRejectionCooldownUntil != null && current.isBefore(lastRejectionCooldownUntil)) {
            long remainingMinutes = Duration.between(current, lastRejectionCooldownUntil).toMinutes();
            throw new DomainException("RETAKE_COOLDOWN_ACTIVE",
                    "Yêu cầu trước đó bị từ chối. Vui lòng đợi thêm " + remainingMinutes + " phút (Cooldown 12 giờ) trước khi gửi lại");
        }

        UUID requestId = UUID.randomUUID();
        int newCount = priorRequestsCount + 1;
        ExamRetakeRequest request = new ExamRetakeRequest(
                requestId,
                examConfigId,
                studentId,
                newCount,
                reason,
                RetakeRequestStatus.PENDING,
                null,
                null,
                null,
                "",
                current,
                null
        );

        request.registerDomainEvent(ExamRetakeRequestedEvent.of(requestId, examConfigId, studentId, newCount));
        return request;
    }

    /**
     * Giáo viên phê duyệt cho học sinh thi lại.
     */
    public void approve(UUID teacherId, Duration validityDuration, String comment, Instant now) {
        if (this.status != RetakeRequestStatus.PENDING) {
            throw new DomainException("INVALID_STATUS", "Chỉ có thể phê duyệt yêu cầu đang ở trạng thái PENDING");
        }
        Duration duration = (validityDuration != null && !validityDuration.isNegative() && !validityDuration.isZero())
                ? validityDuration : Duration.ofHours(24);

        Instant current = now != null ? now : Instant.now();
        this.status = RetakeRequestStatus.APPROVED;
        this.reviewedByTeacherId = teacherId;
        this.grantExpiresAt = current.plus(duration);
        this.reviewComment = comment != null ? comment.trim() : "";
        this.reviewedAt = current;

        registerDomainEvent(ExamRetakeApprovedEvent.of(this.id, this.examConfigId, this.studentId, teacherId, this.grantExpiresAt));
    }

    /**
     * Giáo viên từ chối yêu cầu xin thi lại (kích hoạt Cooldown 12 giờ).
     */
    public void reject(UUID teacherId, String reason, Instant now) {
        if (this.status != RetakeRequestStatus.PENDING) {
            throw new DomainException("INVALID_STATUS", "Chỉ có thể từ chối yêu cầu đang ở trạng thái PENDING");
        }
        Instant current = now != null ? now : Instant.now();
        this.status = RetakeRequestStatus.REJECTED;
        this.reviewedByTeacherId = teacherId;
        this.reviewComment = reason != null ? reason.trim() : "Không chấp nhận yêu cầu";
        this.rejectionCooldownUntil = current.plus(REJECTION_COOLDOWN);
        this.reviewedAt = current;
    }

    /**
     * Kiểm tra và đánh dấu hết hạn lượt thi lại nếu học sinh không làm bài trong thời gian quy định.
     */
    public void checkExpiration(Instant now) {
        if (this.status == RetakeRequestStatus.APPROVED && this.grantExpiresAt != null && now.isAfter(this.grantExpiresAt)) {
            this.status = RetakeRequestStatus.EXPIRED;
        }
    }

    public UUID getId() { return id; }
    public UUID getExamConfigId() { return examConfigId; }
    public UUID getStudentId() { return studentId; }
    public int getRequestCount() { return requestCount; }
    public String getReason() { return reason; }
    public RetakeRequestStatus getStatus() { return status; }
    public Instant getRejectionCooldownUntil() { return rejectionCooldownUntil; }
    public Instant getGrantExpiresAt() { return grantExpiresAt; }
    public UUID getReviewedByTeacherId() { return reviewedByTeacherId; }
    public String getReviewComment() { return reviewComment; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReviewedAt() { return reviewedAt; }
}
