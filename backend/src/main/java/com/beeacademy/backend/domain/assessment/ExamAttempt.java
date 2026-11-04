package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.assessment.event.ExamAttemptSubmittedEvent;
import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho một lượt làm bài thi kiểm tra của học sinh.
 * Theo dõi tính trung thực khi thi (ExamIntegrityEvent) và chấm điểm kết quả.
 */
public class ExamAttempt extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID examConfigId;
    private final UUID studentId;
    private final int attemptNo;
    private ScorePercent score;
    private boolean passed;
    private AttemptStatus status;
    private final Instant startedAt;
    private Instant submittedAt;
    private String answersJson;
    private final List<ExamIntegrityEvent> integrityEvents;

    public ExamAttempt(UUID id,
                       UUID examConfigId,
                       UUID studentId,
                       int attemptNo,
                       ScorePercent score,
                       boolean passed,
                       AttemptStatus status,
                       Instant startedAt,
                       Instant submittedAt,
                       String answersJson,
                       List<ExamIntegrityEvent> integrityEvents) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (examConfigId == null) throw new DomainException("INVALID_CONFIG_ID", "ExamConfig ID không được null");
        if (studentId == null) throw new DomainException("INVALID_STUDENT_ID", "Student ID không được null");
        if (attemptNo < 1) throw new DomainException("INVALID_ATTEMPT_NO", "Số thứ tự lần thi phải >= 1");

        this.id = id;
        this.examConfigId = examConfigId;
        this.studentId = studentId;
        this.attemptNo = attemptNo;
        this.score = score != null ? score : ScorePercent.ZERO;
        this.passed = passed;
        this.status = status != null ? status : AttemptStatus.IN_PROGRESS;
        this.startedAt = startedAt != null ? startedAt : Instant.now();
        this.submittedAt = submittedAt;
        this.answersJson = answersJson != null ? answersJson : "{}";
        this.integrityEvents = integrityEvents != null ? new ArrayList<>(integrityEvents) : new ArrayList<>();
    }

    public static ExamAttempt start(UUID studentId, UUID examConfigId, int attemptNo) {
        return new ExamAttempt(
                UUID.randomUUID(),
                examConfigId,
                studentId,
                attemptNo,
                ScorePercent.ZERO,
                false,
                AttemptStatus.IN_PROGRESS,
                Instant.now(),
                null,
                "{}",
                new ArrayList<>()
        );
    }

    public void recordIntegrityEvent(IntegrityEventType eventType, String details) {
        if (this.status != AttemptStatus.IN_PROGRESS) {
            return;
        }
        this.integrityEvents.add(ExamIntegrityEvent.record(this.id, eventType, details));
    }

    public void submit(ScorePercent calculatedScore, ScorePercent passingThreshold, String answersJson, Instant now) {
        if (this.status != AttemptStatus.IN_PROGRESS) {
            throw new DomainException("ATTEMPT_NOT_IN_PROGRESS", "Lượt làm bài đã kết thúc trước đó");
        }
        if (calculatedScore == null) {
            throw new DomainException("INVALID_SCORE", "Điểm số không được null");
        }

        this.score = calculatedScore;
        this.passed = passingThreshold != null && calculatedScore.isPassed(passingThreshold);
        this.status = AttemptStatus.SUBMITTED;
        this.submittedAt = now != null ? now : Instant.now();
        this.answersJson = answersJson != null ? answersJson : "{}";

        registerDomainEvent(ExamAttemptSubmittedEvent.of(this.id, this.examConfigId, this.studentId, this.score, this.passed));
    }

    public UUID getId() { return id; }
    public UUID getExamConfigId() { return examConfigId; }
    public UUID getStudentId() { return studentId; }
    public int getAttemptNo() { return attemptNo; }
    public ScorePercent getScore() { return score; }
    public boolean isPassed() { return passed; }
    public AttemptStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public String getAnswersJson() { return answersJson; }
    public List<ExamIntegrityEvent> getIntegrityEvents() { return Collections.unmodifiableList(integrityEvents); }
}
