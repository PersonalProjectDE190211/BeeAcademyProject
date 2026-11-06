package com.beeacademy.backend.api.learning.dto;

import jakarta.validation.constraints.Min;

public record TrackProgressRequest(
        @Min(value = 0, message = "Thời điểm bắt đầu phải >= 0")
        int startSec,

        @Min(value = 0, message = "Thời điểm kết thúc phải >= 0")
        int endSec,

        @Min(value = 0, message = "Vị trí player hiện tại phải >= 0")
        int currentPositionSec
) {
}
