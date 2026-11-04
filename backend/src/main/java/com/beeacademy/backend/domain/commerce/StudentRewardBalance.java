package com.beeacademy.backend.domain.commerce;

import com.beeacademy.backend.domain.commerce.event.RewardAwardedDomainEvent;
import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Điểm thưởng thành tích của Học sinh.
 * Điểm thưởng được cộng khi đạt điểm cao trong kỳ thi/quiz và có thể dùng để đổi voucher.
 */
public class StudentRewardBalance extends AbstractAggregateRoot {

    private final UUID id;
    private final UUID studentId;
    private int points;
    private Instant updatedAt;

    public StudentRewardBalance(UUID id, UUID studentId, int points, Instant updatedAt) {
        if (id == null) {
            throw new DomainException("INVALID_REWARD_BALANCE", "ID không được null");
        }
        if (studentId == null) {
            throw new DomainException("INVALID_REWARD_BALANCE", "Student ID không được null");
        }
        if (points < 0) {
            throw new DomainException("NEGATIVE_POINTS", "Điểm thưởng không được là số âm");
        }
        this.id = id;
        this.studentId = studentId;
        this.points = points;
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static StudentRewardBalance init(UUID studentId) {
        return new StudentRewardBalance(UUID.randomUUID(), studentId, 0, Instant.now());
    }

    public void awardPoints(int pointsToAdd, String reason) {
        if (pointsToAdd <= 0) {
            throw new DomainException("INVALID_POINTS", "Số điểm thưởng thêm phải lớn hơn 0");
        }
        this.points = Math.addExact(this.points, pointsToAdd);
        this.updatedAt = Instant.now();
        registerDomainEvent(new RewardAwardedDomainEvent(this.studentId, pointsToAdd, this.points, reason));
    }

    public void redeemPoints(int pointsToRedeem, String reason) {
        if (pointsToRedeem <= 0) {
            throw new DomainException("INVALID_POINTS", "Số điểm quy đổi phải lớn hơn 0");
        }
        if (this.points < pointsToRedeem) {
            throw new DomainException("INSUFFICIENT_POINTS",
                    String.format("Không đủ điểm thưởng để quy đổi: hiện có %d, yêu cầu %d", this.points, pointsToRedeem));
        }
        this.points -= pointsToRedeem;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public int getPoints() {
        return points;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentRewardBalance that = (StudentRewardBalance) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
