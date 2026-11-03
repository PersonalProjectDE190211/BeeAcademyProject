package com.beeacademy.backend.domain.auth;

import com.beeacademy.backend.domain.auth.event.AccountLockedEvent;
import com.beeacademy.backend.domain.auth.event.TeacherApprovedEvent;
import com.beeacademy.backend.domain.auth.event.UserRegisteredEvent;
import com.beeacademy.backend.domain.common.DomainEvent;
import com.beeacademy.backend.domain.common.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Domain Auth (Profile Aggregate Root, PasswordHash & Lockout)")
class ProfileDomainTest {

    private static final PasswordHash SAMPLE_HASH = PasswordHash.of("$2a$12$e8Y5Z4x7u.vK6zX3Y8a8de7jK5l4m3n2o1p0q9r8s7t6u5v4w3x2y");

    @Test
    @DisplayName("Tạo mới Profile học sinh: Lưu password băm BCrypt, sinh UserRegisteredEvent")
    void testCreateNewStudentProfile() {
        UUID userId = UUID.randomUUID();
        Profile profile = Profile.createNew(userId, "student@bee.edu.vn", SAMPLE_HASH, UserRole.STUDENT, "Nguyễn Văn A");

        assertEquals(userId, profile.getId());
        assertEquals("student@bee.edu.vn", profile.getEmail());
        assertEquals(SAMPLE_HASH, profile.getPasswordHash());
        assertEquals(UserRole.STUDENT, profile.getRole());
        assertEquals("Nguyễn Văn A", profile.getFullName());
        assertEquals(TeacherApprovalStatus.NONE, profile.getTeacherApprovalStatus());
        assertFalse(profile.isMustChangePassword());
        assertFalse(profile.isBlocked());

        List<DomainEvent> events = profile.pollDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof UserRegisteredEvent);
    }

    @Test
    @DisplayName("Admin cấp tài khoản Giáo viên (V046): Duyệt APPROVED ngay và bắt buộc đổi mật khẩu tạm")
    void testCreateTeacherByAdmin() {
        UUID teacherId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Profile teacher = Profile.createTeacherByAdmin(
                teacherId,
                "teacher@bee.edu.vn",
                SAMPLE_HASH,
                "Trần Thị B",
                adminId,
                "Cấp qua Zalo 0912345678"
        );

        assertEquals(UserRole.TEACHER, teacher.getRole());
        assertEquals(TeacherApprovalStatus.APPROVED, teacher.getTeacherApprovalStatus());
        assertTrue(teacher.isMustChangePassword(), "Tài khoản GV do Admin cấp phải có mustChangePassword = true");
        assertEquals(adminId, teacher.getCreatedByAdminId());
        assertEquals("Cấp qua Zalo 0912345678", teacher.getTeacherContactNote());

        List<DomainEvent> events = teacher.pollDomainEvents();
        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(e -> e instanceof UserRegisteredEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof TeacherApprovedEvent));

        // Sau khi GV đổi mật khẩu lần đầu
        PasswordHash newHash = PasswordHash.of("$2a$12$newPasswordHashVeryStrong1234567890abcdefghijklmnopqr");
        teacher.updatePassword(newHash);
        assertEquals(newHash, teacher.getPasswordHash());
        assertFalse(teacher.isMustChangePassword(), "Sau khi đổi mật khẩu, mustChangePassword phải tự tắt");
    }

    @Test
    @DisplayName("Cập nhật thông tin cá nhân: Validate họ tên không được trống")
    void testUpdatePersonalInfo() {
        Profile profile = Profile.createNew(UUID.randomUUID(), "user@bee.edu.vn", SAMPLE_HASH, UserRole.STUDENT, "Văn A");

        profile.updatePersonalInfo("Nguyễn Văn A Mới", "0988777666", "Yêu thích Toán học", null);
        assertEquals("Nguyễn Văn A Mới", profile.getFullName());
        assertEquals("0988777666", profile.getPhone());
        assertEquals("Yêu thích Toán học", profile.getBio());

        // Họ tên rỗng phải ném DomainException
        assertThrows(DomainException.class, () -> profile.updatePersonalInfo("   ", null, null, null));
    }

    @Test
    @DisplayName("Bảo mật REQ-AUTH-002: Khóa tạm 15 phút sau 5 lần đăng nhập sai liên tiếp")
    void testFailedLoginLockoutAfter5Attempts() {
        Profile profile = Profile.createNew(UUID.randomUUID(), "victim@bee.edu.vn", SAMPLE_HASH, UserRole.STUDENT, "Học sinh");
        Instant now = Instant.now();

        // 4 lần đăng nhập sai đầu tiên: Chưa khóa
        for (int i = 1; i <= 4; i++) {
            profile.registerFailedLogin(now.plusSeconds(i * 10));
            assertFalse(profile.isTemporarilyLocked(now.plusSeconds(i * 10)));
            assertEquals(i, profile.getLockoutState().failedAttempts());
        }

        // Lần thứ 5 sai: Bị khóa tạm thời 15 phút
        Instant fifthAttemptTime = now.plusSeconds(50);
        profile.registerFailedLogin(fifthAttemptTime);

        assertTrue(profile.isTemporarilyLocked(fifthAttemptTime));
        assertEquals(5, profile.getLockoutState().failedAttempts());
        assertNotNull(profile.getLockoutState().lockedUntil());
        assertTrue(profile.remainingLockSeconds(fifthAttemptTime) > 0);

        // Sinh sự kiện AccountLockedEvent
        assertTrue(profile.pollDomainEvents().stream().anyMatch(e -> e instanceof AccountLockedEvent));

        // Kiểm tra sau 16 phút: Tự động hết khóa
        Instant sixteenMinutesLater = fifthAttemptTime.plus(16, ChronoUnit.MINUTES);
        assertFalse(profile.isTemporarilyLocked(sixteenMinutesLater));
    }

    @Test
    @DisplayName("Bảo mật REQ-AUTH-002: Lần sai cách hơn 15 phút không tính là liên tiếp (đếm lại từ 1)")
    void testFailedLoginCounterResetsAfterWindow() {
        Profile profile = Profile.createNew(UUID.randomUUID(), "user@bee.edu.vn", SAMPLE_HASH, UserRole.STUDENT, "Học sinh");
        Instant t0 = Instant.now();

        // Sai 3 lần
        profile.registerFailedLogin(t0);
        profile.registerFailedLogin(t0.plusSeconds(5));
        profile.registerFailedLogin(t0.plusSeconds(10));
        assertEquals(3, profile.getLockoutState().failedAttempts());

        // Lần sai tiếp theo sau 20 phút (quá cửa sổ 15 phút) -> Bộ đếm đặt lại về 1
        Instant tAfter20Mins = t0.plus(20, ChronoUnit.MINUTES);
        profile.registerFailedLogin(tAfter20Mins);
        assertEquals(1, profile.getLockoutState().failedAttempts());
        assertFalse(profile.isTemporarilyLocked(tAfter20Mins));
    }

    @Test
    @DisplayName("Đăng nhập thành công: Đặt lại toàn bộ bộ đếm sai về 0")
    void testResetFailedLoginOnSuccess() {
        Profile profile = Profile.createNew(UUID.randomUUID(), "user@bee.edu.vn", SAMPLE_HASH, UserRole.STUDENT, "Học sinh");
        Instant now = Instant.now();

        profile.registerFailedLogin(now);
        profile.registerFailedLogin(now.plusSeconds(5));
        assertEquals(2, profile.getLockoutState().failedAttempts());

        // Đăng nhập thành công -> Reset
        profile.resetFailedLogin();
        assertEquals(0, profile.getLockoutState().failedAttempts());
        assertNull(profile.getLockoutState().lockedUntil());
    }
}
