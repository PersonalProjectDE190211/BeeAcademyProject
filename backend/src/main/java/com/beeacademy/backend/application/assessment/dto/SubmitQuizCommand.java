package com.beeacademy.backend.application.assessment.dto;

import java.util.Map;
import java.util.UUID;

public record SubmitQuizCommand(
        UUID attemptId,
        UUID studentId,
        Map<UUID, UUID> answers
) {
}
