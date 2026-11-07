package com.beeacademy.backend.application.family.dto;

import java.util.UUID;

public record AcceptLinkCommand(
        UUID studentId,
        String inviteToken
) {
    public AcceptLinkCommand {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID không được để trống");
        }
        if (inviteToken == null || inviteToken.isBlank()) {
            throw new IllegalArgumentException("Mã lời mời không được để trống");
        }
    }
}

