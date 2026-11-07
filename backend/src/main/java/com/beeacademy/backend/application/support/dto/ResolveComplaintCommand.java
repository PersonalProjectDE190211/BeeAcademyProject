package com.beeacademy.backend.application.support.dto;

import java.util.UUID;

public record ResolveComplaintCommand(
        UUID complaintId,
        UUID adminId,
        String resolutionSummary
) {
    public ResolveComplaintCommand {
        if (complaintId == null) {
            throw new IllegalArgumentException("Complaint ID không được để trống");
        }
        if (adminId == null) {
            throw new IllegalArgumentException("Admin ID không được để trống");
        }
        if (resolutionSummary == null || resolutionSummary.isBlank()) {
            throw new IllegalArgumentException("Tóm tắt hướng giải quyết không được để trống");
        }
    }
}

