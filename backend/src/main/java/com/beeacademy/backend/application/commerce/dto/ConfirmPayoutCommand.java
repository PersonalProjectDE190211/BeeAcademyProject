package com.beeacademy.backend.application.commerce.dto;

import java.util.UUID;

public record ConfirmPayoutCommand(
        UUID adminId,
        UUID payoutPeriodId,
        String transferRef,
        String transferContent,
        String uncAttachmentUrl
) {
    public ConfirmPayoutCommand {
        if (adminId == null) {
            throw new IllegalArgumentException("Admin ID không được để trống");
        }
        if (payoutPeriodId == null) {
            throw new IllegalArgumentException("Payout Period ID không được để trống");
        }
        if (transferRef == null || transferRef.isBlank()) {
            throw new IllegalArgumentException("Mã giao dịch ngân hàng không được để trống");
        }
    }
}

