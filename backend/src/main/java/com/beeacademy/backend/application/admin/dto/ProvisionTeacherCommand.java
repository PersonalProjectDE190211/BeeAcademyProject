package com.beeacademy.backend.application.admin.dto;

public record ProvisionTeacherCommand(
        String email,
        String temporaryPassword,
        String fullName,
        String contactNote
) {
}

