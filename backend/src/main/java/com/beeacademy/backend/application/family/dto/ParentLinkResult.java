package com.beeacademy.backend.application.family.dto;

import java.time.Instant;
import java.util.UUID;

public record ParentLinkResult(
        UUID id,
        UUID parentId,
        UUID studentId,
        String studentEmail,
        String inviteToken,
        String status,
        boolean consentGranted,
        Instant inviteExpiresAt,
        Instant linkedAt,
        Instant unlinkedAt,
        String unlinkReason
) {}

