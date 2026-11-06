package com.beeacademy.backend.application.auth.dto;

public record ResetPasswordCommand(
        String email,
        String otpOrToken,
        String newPassword
) {
}

