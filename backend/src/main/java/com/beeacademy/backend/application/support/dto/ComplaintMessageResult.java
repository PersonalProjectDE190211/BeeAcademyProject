package com.beeacademy.backend.application.support.dto;

import java.time.Instant;
import java.util.UUID;

public record ComplaintMessageResult(
        UUID id,
        UUID authorId,
        String authorRole,
        String content,
        Instant createdAt
) {}

