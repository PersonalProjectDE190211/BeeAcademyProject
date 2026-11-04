package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.assessment.event.AssignmentGradedEvent;
import com.beeacademy.backend.domain.assessment.event.AssignmentSubmittedEvent;
import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Bài nộp tự luận của học sinh (UC16).
 * Quản lý lịch sử chấm điểm (GradeAuditLog) do Giáo viên chấm tay hoặc AI chấm tự động.
 */
public class AssignmentSubmission extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID assignmentId;
    private final UUID studentId;
    private String submissionText;
    private String attachmentUrl;
    private ScorePercent score;
    private SubmissionStatus status;
    private final Instant submittedAt;
    private Instant gradedAt;
    private UUID gradedById;
    private String feedback;
    private final List<GradeAuditLog> gradeAuditLogs;

    public AssignmentSubmission(UUID id,
                                UUID assignmentId,
                                UUID studentId,
                                String submissionText,
                                String attachmentUrl,
                                ScorePercent score,
                                SubmissionStatus status,
                                Instant submittedAt,
                                Instant gradedAt,
                                UUID gradedById,
                                String feedback,
                                List<GradeAuditLog> gradeAuditLogs) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (assignmentId == null) throw new DomainException("INVALID_ASSIGNMENT_ID", "Assignment ID không được null");
        if (studentId == null) throw new DomainException("INVALID_STUDENT_ID", "Student ID không được null");
        if ((submissionText == null || submissionText.isBlank()) && (attachmentUrl == null || attachmentUrl.isBlank())) {
            throw new DomainException("INVALID_SUBMISSION", "Bài nộp phải có nội dung văn bản hoặc file đính kèm");
        }

        this.id = id;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.submissionText = submissionText != null ? submissionText.trim() : "";
        this.attachmentUrl = attachmentUrl != null ? attachmentUrl.trim() : "";
        this.score = score;
        this.status = status != null ? status : SubmissionStatus.SUBMITTED;
        this.submittedAt = submittedAt != null ? submittedAt : Instant.now();
        this.gradedAt = gradedAt;
        this.gradedById = gradedById;
        this.feedback = feedback != null ? feedback.trim() : "";
        this.gradeAuditLogs = gradeAuditLogs != null ? new ArrayList<>(gradeAuditLogs) : new ArrayList<>();
    }

    public static AssignmentSubmission submit(UUID studentId, UUID assignmentId, String submissionText, String attachmentUrl) {
        UUID submissionId = UUID.randomUUID();
        AssignmentSubmission submission = new AssignmentSubmission(
                submissionId,
                assignmentId,
                studentId,
                submissionText,
                attachmentUrl,
                null,
                SubmissionStatus.SUBMITTED,
                Instant.now(),
                null,
                null,
                "",
                new ArrayList<>()
        );

        submission.registerDomainEvent(AssignmentSubmittedEvent.of(submissionId, assignmentId, studentId));
        return submission;
    }

    /**
     * Chấm điểm bài nộp (do Giáo viên hoặc AI thực hiện).
     */
    public void grade(UUID graderId, boolean isAi, ScorePercent awardedScore, String feedback, Instant now) {
        if (graderId == null) throw new DomainException("INVALID_GRADER_ID", "Grader ID không được null");
        if (awardedScore == null) throw new DomainException("INVALID_SCORE", "Điểm số không được null");

        Instant current = now != null ? now : Instant.now();
        this.score = awardedScore;
        this.status = SubmissionStatus.GRADED;
        this.gradedById = graderId;
        this.gradedAt = current;
        this.feedback = feedback != null ? feedback.trim() : "";

        this.gradeAuditLogs.add(GradeAuditLog.of(this.id, graderId, isAi, awardedScore, this.feedback));
        registerDomainEvent(AssignmentGradedEvent.of(this.id, this.assignmentId, this.studentId, graderId, isAi, awardedScore));
    }

    public UUID getId() { return id; }
    public UUID getAssignmentId() { return assignmentId; }
    public UUID getStudentId() { return studentId; }
    public String getSubmissionText() { return submissionText; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public ScorePercent getScore() { return score; }
    public SubmissionStatus getStatus() { return status; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getGradedAt() { return gradedAt; }
    public UUID getGradedById() { return gradedById; }
    public String getFeedback() { return feedback; }
    public List<GradeAuditLog> getGradeAuditLogs() { return Collections.unmodifiableList(gradeAuditLogs); }
}
