package com.beeacademy.backend.application.auth.dto;

import com.beeacademy.backend.domain.auth.Profile;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResult(
        UUID id,
        String email,
        String role,
        String fullName,
        String avatarUrl,
        String phone,
        String bio,
        String teacherApprovalStatus,
        boolean isBlocked,
        Instant createdAt
) {
    public static UserProfileResult from(Profile profile) {
        return new UserProfileResult(
                profile.getId(),
                profile.getEmail(),
                profile.getRole().getDbValue(),
                profile.getFullName(),
                profile.getAvatarUrl(),
                profile.getPhone(),
                profile.getBio(),
                profile.getTeacherApprovalStatus() != null ? profile.getTeacherApprovalStatus().name().toLowerCase() : "none",
                profile.isBlocked(),
                profile.getCreatedAt()
        );
    }
}
