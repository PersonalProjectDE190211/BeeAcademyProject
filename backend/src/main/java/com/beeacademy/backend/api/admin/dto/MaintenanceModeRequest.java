package com.beeacademy.backend.api.admin.dto;

public record MaintenanceModeRequest(
        boolean enabled,
        String message
) {
}

