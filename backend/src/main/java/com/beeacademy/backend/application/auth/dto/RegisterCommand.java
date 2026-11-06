package com.beeacademy.backend.application.auth.dto;

import com.beeacademy.backend.domain.auth.UserRole;

public record RegisterCommand(
        String email,
        String password,
        String fullName,
        UserRole role
) {
}

