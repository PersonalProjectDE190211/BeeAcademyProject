package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.assessment.event.QuizAttemptSubmittedEvent;
import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho một lượt làm bài quiz chương của học sinh.
 */
public class QuizAttempt extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID studentId;
    private final UUID quizConfigId;
    private final UUID chapterId;
    private ScorePercent score;
    private boolean passed;
    private AttemptStatus status;
    private final Instant startedAt;
    private Instant submittedAt;

    public QuizAttempt(UUID id,
                       UUID studentId,
                       UUID quizConfigId,
                       UUID chapterId,
                       ScorePercent score,
                       boolean passed,
                       AttemptStatus status,
                       Instant startedAt,
                       Instant submittedAt) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (studentId == null) throw new DomainException("INVALID_STUDENT_ID", "Student ID không được null");
        if (quizConfigId == null) throw new DomainException("INVALID_CONFIG_ID", "QuizConfig ID không được null");
        if (chapterId == null) throw new DomainException("INVALID_CHAPTER_ID", "Chapter ID không được null");

        this.id = id;
        this.studentId = studentId;
        this.quizConfigId = quizConfigId;
        this.chapterId = chapterId;
        this.score = score != null ? score : ScorePercent.ZERO;
        this.passed = passed;
        this.status = status != null ? status : AttemptStatus.IN_PROGRESS;
        this.startedAt = startedAt != null ? startedAt : Instant.now();
        this.submittedAt = submittedAt;
    }

    public static QuizAttempt start(UUID studentId, UUID quizConfigId, UUID chapterId) {
        return new QuizAttempt(
                UUID.randomUUID(),
                studentId,
                quizConfigId,
                chapterId,
                ScorePercent.ZERO,
                false,
                AttemptStatus.IN_PROGRESS,
                Instant.now(),
                null
        );
    }

    public void submit(ScorePercent calculatedScore, ScorePercent passingThreshold, Instant now) {
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

        registerDomainEvent(QuizAttemptSubmittedEvent.of(this.id, this.studentId, this.chapterId, this.score, this.passed));
    }

    public UUID getId() { return id; }
    public UUID getStudentId() { return studentId; }
    public UUID getQuizConfigId() { return quizConfigId; }
    public UUID getChapterId() { return chapterId; }
    public ScorePercent getScore() { return score; }
    public boolean isPassed() { return passed; }
    public AttemptStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getSubmittedAt() { return submittedAt; }
}
