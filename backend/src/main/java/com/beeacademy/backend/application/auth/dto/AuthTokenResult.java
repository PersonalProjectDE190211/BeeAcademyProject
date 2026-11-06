package com.beeacademy.backend.application.auth.dto;

public record AuthTokenResult(
        String accessToken,
        String refreshToken,
        UserProfileResult user
) {
}

