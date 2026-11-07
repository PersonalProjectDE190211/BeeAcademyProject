package com.beeacademy.backend.api.commerce.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPayoutRequest(
        @NotBlank(message = "Mã giao dịch ngân hàng không được để trống")
        String transferRef,

        String transferContent,

        String uncAttachmentUrl
) {}

