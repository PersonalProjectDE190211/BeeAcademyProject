package com.beeacademy.backend.api.assessment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public record SubmitQuizRequest(
        @NotNull(message = "Attempt ID không được null")
        UUID attemptId,
        Map<UUID, UUID> answers
) {
}
