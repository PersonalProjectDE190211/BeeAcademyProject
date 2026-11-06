package com.beeacademy.backend.infrastructure.assessment.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quiz_configs")
public class QuizConfigJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "chapter_id", nullable = false, unique = true)
    private UUID chapterId;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    @Column(name = "easy_count", nullable = false)
    private int easyCount;

    @Column(name = "medium_count", nullable = false)
    private int mediumCount;

    @Column(name = "hard_count", nullable = false)
    private int hardCount;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @Column(name = "passing_score", nullable = false)
    private BigDecimal passingScore;

    @Column(name = "shuffle_questions", nullable = false)
    private boolean shuffleQuestions;

    @Column(name = "shuffle_choices", nullable = false)
    private boolean shuffleChoices;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public QuizConfigJpaEntity() {
    }

    public QuizConfigJpaEntity(UUID id, UUID chapterId, UUID teacherId, int totalQuestions,
                               int easyCount, int mediumCount, int hardCount, Integer timeLimitMinutes,
                               BigDecimal passingScore, boolean shuffleQuestions, boolean shuffleChoices,
                               Integer maxAttempts, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.chapterId = chapterId;
        this.teacherId = teacherId;
        this.totalQuestions = totalQuestions;
        this.easyCount = easyCount;
        this.mediumCount = mediumCount;
        this.hardCount = hardCount;
        this.timeLimitMinutes = timeLimitMinutes;
        this.passingScore = passingScore;
        this.shuffleQuestions = shuffleQuestions;
        this.shuffleChoices = shuffleChoices;
        this.maxAttempts = maxAttempts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getChapterId() { return chapterId; }
    public void setChapterId(UUID chapterId) { this.chapterId = chapterId; }

    public UUID getTeacherId() { return teacherId; }
    public void setTeacherId(UUID teacherId) { this.teacherId = teacherId; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getEasyCount() { return easyCount; }
    public void setEasyCount(int easyCount) { this.easyCount = easyCount; }

    public int getMediumCount() { return mediumCount; }
    public void setMediumCount(int mediumCount) { this.mediumCount = mediumCount; }

    public int getHardCount() { return hardCount; }
    public void setHardCount(int hardCount) { this.hardCount = hardCount; }

    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }

    public BigDecimal getPassingScore() { return passingScore; }
    public void setPassingScore(BigDecimal passingScore) { this.passingScore = passingScore; }

    public boolean isShuffleQuestions() { return shuffleQuestions; }
    public void setShuffleQuestions(boolean shuffleQuestions) { this.shuffleQuestions = shuffleQuestions; }

    public boolean isShuffleChoices() { return shuffleChoices; }
    public void setShuffleChoices(boolean shuffleChoices) { this.shuffleChoices = shuffleChoices; }

    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
