package com.beeacademy.backend.application.family.dto;

import java.util.UUID;

public record RejectLinkCommand(
        UUID studentId,
        String inviteToken,
        String reason
) {
    public RejectLinkCommand {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID không được để trống");
        }
        if (inviteToken == null || inviteToken.isBlank()) {
            throw new IllegalArgumentException("Mã lời mời không được để trống");
        }
    }
}

