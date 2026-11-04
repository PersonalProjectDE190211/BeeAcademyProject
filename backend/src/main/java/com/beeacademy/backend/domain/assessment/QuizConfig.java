package com.beeacademy.backend.domain.assessment;

import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.common.ScorePercent;

import java.util.UUID;

/**
 * Aggregate Root đại diện cho Cấu hình bài kiểm tra trắc nghiệm (Quiz) của một chương.
 * Tự động kiểm chứng bất biến: easyCount + mediumCount + hardCount == totalQuestions.
 */
public class QuizConfig extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID chapterId;
    private final UUID teacherId;
    private int totalQuestions;
    private int easyCount;
    private int mediumCount;
    private int hardCount;
    private Integer timeLimitMinutes;
    private ScorePercent passingScore;
    private boolean shuffleQuestions;
    private boolean shuffleChoices;
    private Integer maxAttempts;

    public QuizConfig(UUID id,
                      UUID chapterId,
                      UUID teacherId,
                      int totalQuestions,
                      int easyCount,
                      int mediumCount,
                      int hardCount,
                      Integer timeLimitMinutes,
                      ScorePercent passingScore,
                      boolean shuffleQuestions,
                      boolean shuffleChoices,
                      Integer maxAttempts) {
        if (id == null) throw new DomainException("INVALID_ID", "ID không được null");
        if (chapterId == null) throw new DomainException("INVALID_CHAPTER_ID", "Chapter ID không được null");
        if (teacherId == null) throw new DomainException("INVALID_TEACHER_ID", "Teacher ID không được null");
        validateDistribution(totalQuestions, easyCount, mediumCount, hardCount);

        this.id = id;
        this.chapterId = chapterId;
        this.teacherId = teacherId;
        this.totalQuestions = totalQuestions;
        this.easyCount = easyCount;
        this.mediumCount = mediumCount;
        this.hardCount = hardCount;
        this.timeLimitMinutes = timeLimitMinutes;
        this.passingScore = passingScore != null ? passingScore : ScorePercent.of(60.0);
        this.shuffleQuestions = shuffleQuestions;
        this.shuffleChoices = shuffleChoices;
        this.maxAttempts = maxAttempts;
    }

    public static QuizConfig create(UUID chapterId,
                                    UUID teacherId,
                                    int totalQuestions,
                                    int easyCount,
                                    int mediumCount,
                                    int hardCount,
                                    Integer timeLimitMinutes,
                                    ScorePercent passingScore,
                                    boolean shuffleQuestions,
                                    boolean shuffleChoices,
                                    Integer maxAttempts) {
        return new QuizConfig(
                UUID.randomUUID(),
                chapterId,
                teacherId,
                totalQuestions,
                easyCount,
                mediumCount,
                hardCount,
                timeLimitMinutes,
                passingScore,
                shuffleQuestions,
                shuffleChoices,
                maxAttempts
        );
    }

    public void update(int totalQuestions,
                       int easyCount,
                       int mediumCount,
                       int hardCount,
                       Integer timeLimitMinutes,
                       ScorePercent passingScore,
                       boolean shuffleQuestions,
                       boolean shuffleChoices,
                       Integer maxAttempts) {
        validateDistribution(totalQuestions, easyCount, mediumCount, hardCount);
        this.totalQuestions = totalQuestions;
        this.easyCount = easyCount;
        this.mediumCount = mediumCount;
        this.hardCount = hardCount;
        this.timeLimitMinutes = timeLimitMinutes;
        this.passingScore = passingScore != null ? passingScore : this.passingScore;
        this.shuffleQuestions = shuffleQuestions;
        this.shuffleChoices = shuffleChoices;
        this.maxAttempts = maxAttempts;
    }

    private static void validateDistribution(int total, int easy, int medium, int hard) {
        if (total <= 0) {
            throw new DomainException("INVALID_TOTAL_QUESTIONS", "Tổng số câu hỏi phải > 0");
        }
        if (easy < 0 || medium < 0 || hard < 0) {
            throw new DomainException("INVALID_DISTRIBUTION", "Số lượng câu hỏi theo độ khó không được âm");
        }
        if (easy + medium + hard != total) {
            throw new DomainException("DISTRIBUTION_MISMATCH",
                    String.format("Tổng phân bổ độ khó (Dễ: %d + TB: %d + Khó: %d = %d) phải bằng tổng số câu (%d)",
                            easy, medium, hard, (easy + medium + hard), total));
        }
    }

    public UUID getId() { return id; }
    public UUID getChapterId() { return chapterId; }
    public UUID getTeacherId() { return teacherId; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getEasyCount() { return easyCount; }
    public int getMediumCount() { return mediumCount; }
    public int getHardCount() { return hardCount; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public ScorePercent getPassingScore() { return passingScore; }
    public boolean isShuffleQuestions() { return shuffleQuestions; }
    public boolean isShuffleChoices() { return shuffleChoices; }
    public Integer getMaxAttempts() { return maxAttempts; }
}
