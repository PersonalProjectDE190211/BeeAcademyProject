package com.beeacademy.backend.domain.auth;

import com.beeacademy.backend.domain.auth.event.AccountLockedEvent;
import com.beeacademy.backend.domain.auth.event.TeacherApprovedEvent;
import com.beeacademy.backend.domain.auth.event.UserRegisteredEvent;
import com.beeacademy.backend.domain.common.AbstractAggregateRoot;
import com.beeacademy.backend.domain.common.DomainException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate Root đại diện cho Hồ sơ người dùng trong hệ sinh thái Bee Academy.
 * Quản lý toàn vẹn các quy tắc:
 * - Lưu trữ mật khẩu băm BCrypt an toàn (PasswordHash).
 * - Khóa tài khoản sau 5 lần đăng nhập sai (SRS REQ-AUTH-002).
 * - Cấp tài khoản giáo viên kèm mật khẩu tạm (SRS V046).
 * - Cập nhật thông tin cá nhân và thay đổi avatar.
 * - Khóa vĩnh viễn (isBlocked) bởi Admin.
 */
public class Profile extends AbstractAggregateRoot {

    private final UUID id;
    private String email;
    private PasswordHash passwordHash;
    private UserRole role;
    private String fullName;
    private String avatarUrl;
    private String phone;
    private String bio;
    private LocalDate dateOfBirth;

    private TeacherApprovalStatus teacherApprovalStatus;
    private boolean mustChangePassword;
    private UUID createdByAdminId;
    private String teacherContactNote;
    private boolean isBlocked;

    private LoginLockoutState lockoutState;

    private Instant createdAt;
    private Instant updatedAt;

    // Package-private constructor for Domain Factory and Persistence rehydration
    protected Profile(UUID id,
                      String email,
                      PasswordHash passwordHash,
                      UserRole role,
                      String fullName,
                      String avatarUrl,
                      String phone,
                      String bio,
                      LocalDate dateOfBirth,
                      TeacherApprovalStatus teacherApprovalStatus,
                      boolean mustChangePassword,
                      UUID createdByAdminId,
                      String teacherContactNote,
                      boolean isBlocked,
                      LoginLockoutState lockoutState,
                      Instant createdAt,
                      Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.phone = phone;
        this.bio = bio;
        this.dateOfBirth = dateOfBirth;
        this.teacherApprovalStatus = teacherApprovalStatus != null ? teacherApprovalStatus : TeacherApprovalStatus.NONE;
        this.mustChangePassword = mustChangePassword;
        this.createdByAdminId = createdByAdminId;
        this.teacherContactNote = teacherContactNote;
        this.isBlocked = isBlocked;
        this.lockoutState = lockoutState != null ? lockoutState : LoginLockoutState.initial();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    // ========================================================================
    // Factory Methods
    // ========================================================================

    /**
     * Tạo mới Profile cho người dùng tự đăng ký (Học sinh, Phụ huynh hoặc Giáo viên).
     */
    public static Profile createNew(UUID authUserId,
                                    String email,
                                    PasswordHash passwordHash,
                                    UserRole role,
                                    String fullName) {
        if (authUserId == null) throw new DomainException("INVALID_USER_ID", "ID người dùng không được null");
        if (role == null) throw new DomainException("INVALID_USER_ROLE", "Vai trò không được null");
        if (passwordHash == null) throw new DomainException("INVALID_PASSWORD_HASH", "Mật khẩu băm không được null");

        TeacherApprovalStatus initialApproval = role == UserRole.TEACHER
                ? TeacherApprovalStatus.PENDING
                : TeacherApprovalStatus.NONE;

        Profile profile = new Profile(
                authUserId,
                email,
                passwordHash,
                role,
                fullName != null ? fullName.trim() : null,
                null,
                null,
                null,
                null,
                initialApproval,
                false,
                null,
                null,
                false,
                LoginLockoutState.initial(),
                Instant.now(),
                Instant.now()
        );

        profile.registerEvent(UserRegisteredEvent.of(authUserId, email, role));
        return profile;
    }

    /**
     * Factory tạo tài khoản Giáo viên do Admin cấp (SRS V046).
     * Trạng thái duyệt là APPROVED ngay, nhưng bật mustChangePassword vì mật khẩu tạm cấp qua kênh ngoài.
     */
    public static Profile createTeacherByAdmin(UUID authUserId,
                                               String email,
                                               PasswordHash temporaryPasswordHash,
                                               String fullName,
                                               UUID adminId,
                                               String contactNote) {
        if (adminId == null) throw new DomainException("INVALID_ADMIN_ID", "Admin ID không được null");
        if (temporaryPasswordHash == null) throw new DomainException("INVALID_PASSWORD_HASH", "Mật khẩu băm tạm không được null");

        Profile profile = new Profile(
                authUserId,
                email,
                temporaryPasswordHash,
                UserRole.TEACHER,
                fullName != null ? fullName.trim() : null,
                null,
                null,
                null,
                null,
                TeacherApprovalStatus.APPROVED,
                true, // Bắt buộc đổi mật khẩu tạm ở lần đăng nhập đầu tiên
                adminId,
                contactNote,
                false,
                LoginLockoutState.initial(),
                Instant.now(),
                Instant.now()
        );

        profile.registerEvent(UserRegisteredEvent.of(authUserId, email, UserRole.TEACHER));
        profile.registerEvent(TeacherApprovedEvent.of(authUserId, adminId));
        return profile;
    }

    /**
     * Dùng để tái tạo (rehydrate) Profile từ Persistence mà không kích hoạt Domain Events.
     */
    public static Profile rehydrate(UUID id,
                                    String email,
                                    PasswordHash passwordHash,
                                    UserRole role,
                                    String fullName,
                                    String avatarUrl,
                                    String phone,
                                    String bio,
                                    LocalDate dateOfBirth,
                                    TeacherApprovalStatus teacherApprovalStatus,
                                    boolean mustChangePassword,
                                    UUID createdByAdminId,
                                    String teacherContactNote,
                                    boolean isBlocked,
                                    LoginLockoutState lockoutState,
                                    Instant createdAt,
                                    Instant updatedAt) {
        return new Profile(id, email, passwordHash, role, fullName, avatarUrl, phone, bio, dateOfBirth,
                teacherApprovalStatus, mustChangePassword, createdByAdminId, teacherContactNote,
                isBlocked, lockoutState, createdAt, updatedAt);
    }

    // ========================================================================
    // Business Methods (State Mutations & Invariants)
    // ========================================================================

    public void updatePassword(PasswordHash newPasswordHash) {
        if (newPasswordHash == null) {
            throw new DomainException("INVALID_PASSWORD", "Mật khẩu mới không được để trống");
        }
        this.passwordHash = newPasswordHash;
        this.mustChangePassword = false; // Đã đổi xong mật khẩu
        this.updatedAt = Instant.now();
    }

    public void updatePersonalInfo(String fullName, String phone, String bio, LocalDate dateOfBirth) {
        if (fullName != null) {
            String trimmed = fullName.trim();
            if (trimmed.isEmpty()) {
                throw new DomainException("INVALID_FULL_NAME", "Họ tên không được để trống");
            }
            this.fullName = trimmed;
        }
        if (phone != null) this.phone = phone.trim();
        if (bio != null) this.bio = bio.trim();
        if (dateOfBirth != null) this.dateOfBirth = dateOfBirth;
        this.updatedAt = Instant.now();
    }

    public void changeAvatar(String newAvatarUrl) {
        this.avatarUrl = newAvatarUrl != null ? newAvatarUrl.trim() : null;
        this.updatedAt = Instant.now();
    }

    public void block() {
        this.isBlocked = true;
        this.updatedAt = Instant.now();
    }

    public void unblock() {
        this.isBlocked = false;
        this.updatedAt = Instant.now();
    }

    public void markMustChangePassword() {
        this.mustChangePassword = true;
        this.updatedAt = Instant.now();
    }

    public void clearMustChangePassword() {
        this.mustChangePassword = false;
        this.updatedAt = Instant.now();
    }

    public void approveTeacher(UUID adminId) {
        if (this.role != UserRole.TEACHER) {
            throw new DomainException("INVALID_ROLE_ACTION", "Chỉ tài khoản giáo viên mới có thể phê duyệt");
        }
        this.teacherApprovalStatus = TeacherApprovalStatus.APPROVED;
        this.updatedAt = Instant.now();
        registerEvent(TeacherApprovedEvent.of(this.id, adminId));
    }

    public void rejectTeacher() {
        if (this.role != UserRole.TEACHER) {
            throw new DomainException("INVALID_ROLE_ACTION", "Chỉ tài khoản giáo viên mới có thể từ chối");
        }
        this.teacherApprovalStatus = TeacherApprovalStatus.REJECTED;
        this.updatedAt = Instant.now();
    }

    // ── Xử lý khóa tài khoản do 5 lần đăng nhập sai (REQ-AUTH-002) ──────────

    public boolean isTemporarilyLocked(Instant now) {
        return this.lockoutState != null && this.lockoutState.isCurrentlyLocked(now);
    }

    public long remainingLockSeconds(Instant now) {
        return this.lockoutState != null ? this.lockoutState.remainingLockSeconds(now) : 0L;
    }

    public void registerFailedLogin(Instant now) {
        if (this.lockoutState == null) {
            this.lockoutState = LoginLockoutState.initial();
        }
        this.lockoutState = this.lockoutState.recordFailedAttempt(now);
        this.updatedAt = now;

        if (this.lockoutState.isCurrentlyLocked(now)) {
            registerEvent(AccountLockedEvent.of(this.id, this.lockoutState.lockedUntil()));
        }
    }

    public void resetFailedLogin() {
        if (this.lockoutState != null && this.lockoutState.failedAttempts() > 0) {
            this.lockoutState = this.lockoutState.reset();
            this.updatedAt = Instant.now();
        }
    }

    // ========================================================================
    // Getters
    // ========================================================================

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public PasswordHash getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPhone() { return phone; }
    public String getBio() { return bio; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public TeacherApprovalStatus getTeacherApprovalStatus() { return teacherApprovalStatus; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public UUID getCreatedByAdminId() { return createdByAdminId; }
    public String getTeacherContactNote() { return teacherContactNote; }
    public boolean isBlocked() { return isBlocked; }
    public LoginLockoutState getLockoutState() { return lockoutState; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
