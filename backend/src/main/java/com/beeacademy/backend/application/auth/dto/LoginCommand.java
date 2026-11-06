package com.beeacademy.backend.application.auth.dto;

public record LoginCommand(
        String email,
        String password,
        String deviceInfo,
        String ipAddress
) {
}

