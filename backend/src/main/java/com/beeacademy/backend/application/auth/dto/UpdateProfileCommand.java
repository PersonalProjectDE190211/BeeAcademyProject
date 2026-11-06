package com.beeacademy.backend.application.auth.dto;

public record UpdateProfileCommand(
        String fullName,
        String phone,
        String bio
) {
}

