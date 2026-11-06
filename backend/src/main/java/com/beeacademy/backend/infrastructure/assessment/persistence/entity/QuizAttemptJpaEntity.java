package com.beeacademy.backend.infrastructure.assessment.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttemptJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "quiz_config_id", nullable = false)
    private UUID quizConfigId;

    @Column(name = "questions_snapshot", columnDefinition = "TEXT")
    private String questionsSnapshot;

    @Column(name = "answers", columnDefinition = "TEXT")
    private String answers;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "passed")
    private Boolean passed;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    public QuizAttemptJpaEntity() {
    }

    public QuizAttemptJpaEntity(UUID id, UUID studentId, UUID quizConfigId, String questionsSnapshot,
                               String answers, BigDecimal score, Boolean passed, int attemptNumber,
                               Instant startedAt, Instant submittedAt) {
        this.id = id;
        this.studentId = studentId;
        this.quizConfigId = quizConfigId;
        this.questionsSnapshot = questionsSnapshot;
        this.answers = answers;
        this.score = score;
        this.passed = passed;
        this.attemptNumber = attemptNumber;
        this.startedAt = startedAt;
        this.submittedAt = submittedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getStudentId() { return studentId; }
    public void setStudentId(UUID studentId) { this.studentId = studentId; }

    public UUID getQuizConfigId() { return quizConfigId; }
    public void setQuizConfigId(UUID quizConfigId) { this.quizConfigId = quizConfigId; }

    public String getQuestionsSnapshot() { return questionsSnapshot; }
    public void setQuestionsSnapshot(String questionsSnapshot) { this.questionsSnapshot = questionsSnapshot; }

    public String getAnswers() { return answers; }
    public void setAnswers(String answers) { this.answers = answers; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
