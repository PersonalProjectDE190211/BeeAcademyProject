package com.beeacademy.backend.application.admin.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AdminUserDetailDto(
        UUID id,
        String email,
        String fullName,
        String avatarUrl,
        String phone,
        String bio,
        LocalDate dateOfBirth,
        String role,
        String status,
        String teacherApprovalStatus,
        boolean emailVerified,
        int failedLoginAttempts,
        Instant lockedUntil,
        boolean mustChangePassword,
        UUID createdByAdminId,
        String teacherContactNote,
        Instant createdAt,
        Instant updatedAt
) {
}

