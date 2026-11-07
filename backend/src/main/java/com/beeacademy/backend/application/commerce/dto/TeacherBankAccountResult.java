package com.beeacademy.backend.application.commerce.dto;

import java.time.Instant;
import java.util.UUID;

public record TeacherBankAccountResult(
        UUID id,
        UUID teacherId,
        String bankName,
        String accountNumberMasked,
        String accountHolder,
        String branch,
        String verifyStatus,
        boolean otpVerified,
        Instant createdAt,
        Instant updatedAt
) {}

