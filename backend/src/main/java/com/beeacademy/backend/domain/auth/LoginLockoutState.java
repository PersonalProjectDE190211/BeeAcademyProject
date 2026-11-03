package com.beeacademy.backend.domain.auth;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Value Object quản lý trạng thái khóa tài khoản tạm thời do đăng nhập sai liên tiếp (SRS REQ-AUTH-002).
 * Quy tắc nghiệp vụ:
 * - Tối đa 5 lần sai liên tiếp trong khung thời gian 15 phút.
 * - Khi đạt 5 lần sai: Tự động khóa tạm thời 15 phút tính từ lần sai cuối cùng.
 * - Nếu lần sai kế tiếp cách lần sai trước hơn 15 phút: Đếm lại từ 1 (không còn tính là liên tiếp).
 */
public record LoginLockoutState(
        int failedAttempts,
        Instant lastFailedAt,
        Instant lockedUntil
) implements Serializable {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final long FAILED_WINDOW_MINUTES = 15L;
    public static final long LOCKOUT_DURATION_MINUTES = 15L;

    public static LoginLockoutState initial() {
        return new LoginLockoutState(0, null, null);
    }

    public static LoginLockoutState of(int failedAttempts, Instant lastFailedAt, Instant lockedUntil) {
        return new LoginLockoutState(failedAttempts, lastFailedAt, lockedUntil);
    }

    /**
     * Kiểm tra tài khoản có đang trong thời gian bị khóa tạm hay không.
     */
    public boolean isCurrentlyLocked(Instant now) {
        return lockedUntil != null && now.isBefore(lockedUntil);
    }

    /**
     * Tính số giây còn lại phải chờ mở khóa. Trả về 0 nếu không bị khóa.
     */
    public long remainingLockSeconds(Instant now) {
        if (!isCurrentlyLocked(now)) {
            return 0L;
        }
        return Math.max(0L, Duration.between(now, lockedUntil).getSeconds());
    }

    /**
     * Ghi nhận một lần đăng nhập thất bại và trả về trạng thái mới.
     */
    public LoginLockoutState recordFailedAttempt(Instant now) {
        int nextAttempts;
        if (lastFailedAt == null || lastFailedAt.isBefore(now.minus(FAILED_WINDOW_MINUTES, ChronoUnit.MINUTES))) {
            // Đã quá 15 phút kể từ lần sai trước -> Đếm lại từ 1
            nextAttempts = 1;
        } else {
            nextAttempts = this.failedAttempts + 1;
        }

        Instant nextLockedUntil = null;
        if (nextAttempts >= MAX_FAILED_ATTEMPTS) {
            nextLockedUntil = now.plus(LOCKOUT_DURATION_MINUTES, ChronoUnit.MINUTES);
        }

        return new LoginLockoutState(nextAttempts, now, nextLockedUntil);
    }

    /**
     * Đặt lại trạng thái khóa về ban đầu sau khi đăng nhập thành công.
     */
    public LoginLockoutState reset() {
        return initial();
    }
}

