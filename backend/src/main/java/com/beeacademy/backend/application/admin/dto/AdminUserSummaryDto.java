package com.beeacademy.backend.application.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminUserSummaryDto(
        UUID id,
        String email,
        String fullName,
        String role,
        String status,
        String teacherApprovalStatus,
        boolean emailVerified,
        Instant createdAt
) {
}

