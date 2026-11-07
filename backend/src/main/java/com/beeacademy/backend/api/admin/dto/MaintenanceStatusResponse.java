package com.beeacademy.backend.api.admin.dto;

public record MaintenanceStatusResponse(
        boolean enabled,
        String message
) {
}

