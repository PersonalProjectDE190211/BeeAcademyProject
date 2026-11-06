package com.beeacademy.backend.application.auth.dto;

public record ChangePasswordCommand(
        String oldPassword,
        String newPassword
) {
}

