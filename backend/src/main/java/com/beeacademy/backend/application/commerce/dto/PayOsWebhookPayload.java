package com.beeacademy.backend.application.commerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PayOsWebhookPayload(
        String code,
        String desc,
        Boolean success,
        PayOsWebhookData data,
        String signature
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PayOsWebhookData(
            Long orderCode,
            Integer amount,
            String description,
            String accountNumber,
            String reference,
            String transactionDateTime,
            String currency,
            String paymentLinkId,
            String code,
            String desc
    ) {}
}

