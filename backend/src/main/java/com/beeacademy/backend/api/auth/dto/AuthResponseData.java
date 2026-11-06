package com.beeacademy.backend.api.auth.dto;

import com.beeacademy.backend.application.auth.dto.UserProfileResult;

public record AuthResponseData(
        String accessToken,
        UserProfileResult user
) {
}

