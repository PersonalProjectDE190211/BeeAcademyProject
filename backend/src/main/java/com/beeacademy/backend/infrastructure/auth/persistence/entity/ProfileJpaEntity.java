package com.beeacademy.backend.infrastructure.auth.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity ánh xạ với bảng 'profiles' trong cơ sở dữ liệu.
 */
@Entity
@Table(name = "profiles")
public class ProfileJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "grade")
    private Integer grade;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "teacher_approval_status", nullable = false, length = 20)
    private String teacherApprovalStatus;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ProfileJpaEntity() {
    }

    public ProfileJpaEntity(UUID id,
                            String email,
                            String passwordHash,
                            String fullName,
                            String avatarUrl,
                            String phone,
                            String bio,
                            String role,
                            Integer grade,
                            String status,
                            String teacherApprovalStatus,
                            boolean emailVerified,
                            int failedLoginAttempts,
                            Instant lockedUntil,
                            Instant createdAt,
                            Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.phone = phone;
        this.bio = bio;
        this.role = role;
        this.grade = grade;
        this.status = status;
        this.teacherApprovalStatus = teacherApprovalStatus;
        this.emailVerified = emailVerified;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTeacherApprovalStatus() { return teacherApprovalStatus; }
    public void setTeacherApprovalStatus(String teacherApprovalStatus) { this.teacherApprovalStatus = teacherApprovalStatus; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

