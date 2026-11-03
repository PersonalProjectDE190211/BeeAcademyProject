package com.beeacademy.backend.domain.common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object biểu diễn điểm số theo tỷ lệ phần trăm (0.0% đến 100.0%).
 * Đóng gói quy tắc làm tròn (scale = 1, HALF_UP) và so sánh ngưỡng đạt (pass/fail).
 */
public record ScorePercent(BigDecimal value) implements Serializable, Comparable<ScorePercent> {

    public static final ScorePercent ZERO = new ScorePercent(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP));
    public static final ScorePercent MAX = new ScorePercent(BigDecimal.valueOf(100.0).setScale(1, RoundingMode.HALF_UP));

    public ScorePercent {
        if (value == null) {
            throw new DomainException("INVALID_SCORE", "Điểm số không được null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(100.0)) > 0) {
            throw new DomainException("SCORE_OUT_OF_RANGE", "Điểm số phải nằm trong khoảng 0.0 đến 100.0 (giá trị nhận: " + value + ")");
        }
    }

    public static ScorePercent of(double score) {
        BigDecimal bd = BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP);
        return new ScorePercent(bd);
    }

    public static ScorePercent of(BigDecimal score) {
        if (score == null) return ZERO;
        return new ScorePercent(score.setScale(1, RoundingMode.HALF_UP));
    }

    /**
     * Tính điểm phần trăm dựa trên số câu đúng và tổng số câu hỏi.
     */
    public static ScorePercent fromRatio(int correctCount, int totalCount) {
        if (totalCount <= 0) return ZERO;
        if (correctCount <= 0) return ZERO;
        if (correctCount >= totalCount) return MAX;

        double percent = ((double) correctCount / totalCount) * 100.0;
        return of(percent);
    }

    /**
     * Kiểm tra đạt ngưỡng điểm đạt (Pass threshold).
     */
    public boolean isPassed(ScorePercent passThreshold) {
        if (passThreshold == null) return true;
        return this.value.compareTo(passThreshold.value) >= 0;
    }

    public boolean isPerfect() {
        return this.value.compareTo(BigDecimal.valueOf(100.0)) >= 0;
    }

    public double toDouble() {
        return this.value.doubleValue();
    }

    @Override
    public int compareTo(ScorePercent o) {
        if (o == null) return 1;
        return this.value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return this.value.toPlainString() + "%";
    }
}

