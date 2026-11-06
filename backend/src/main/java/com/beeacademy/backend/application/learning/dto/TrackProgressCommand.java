package com.beeacademy.backend.application.learning.dto;

import java.util.UUID;

public record TrackProgressCommand(
        UUID studentId,
        UUID courseId,
        UUID lessonId,
        int startSec,
        int endSec,
        int currentPositionSec
) {
}
