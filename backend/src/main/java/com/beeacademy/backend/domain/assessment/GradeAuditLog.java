package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity lưu nhật ký kiểm toán điểm số bài tập (UC35, UC36).
 * Ghi nhận minh bạch điểm số được chấm bởi Giáo viên hay chấm tự động bởi AI.
 */
public class GradeAuditLog {

    private final UUID id;
    private final UUID submissionId;
    private final UUID gradedById;
    private final boolean aiGraded;
    private final ScorePercent score;
    private final String feedback;
    private final Instant occurredAt;

    public GradeAuditLog(UUID id, UUID submissionId, UUID gradedById, boolean aiGraded, ScorePercent score, String feedback, Instant occurredAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (submissionId == null) throw new DomainException("INVALID_SUBMISSION_ID", "Submission ID không được null");
        if (gradedById == null) throw new DomainException("INVALID_GRADER_ID", "Grader ID không được null");
        if (score == null) throw new DomainException("INVALID_SCORE", "Điểm số không được null");

        this.id = id;
        this.submissionId = submissionId;
        this.gradedById = gradedById;
        this.aiGraded = aiGraded;
        this.score = score;
        this.feedback = feedback != null ? feedback.trim() : "";
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }

    public static GradeAuditLog of(UUID submissionId, UUID gradedById, boolean aiGraded, ScorePercent score, String feedback) {
        return new GradeAuditLog(UUID.randomUUID(), submissionId, gradedById, aiGraded, score, feedback, Instant.now());
    }

    public UUID getId() { return id; }
    public UUID getSubmissionId() { return submissionId; }
    public UUID getGradedById() { return gradedById; }
    public boolean isAiGraded() { return aiGraded; }
    public ScorePercent getScore() { return score; }
    public String getFeedback() { return feedback; }
    public Instant getOccurredAt() { return occurredAt; }
}
