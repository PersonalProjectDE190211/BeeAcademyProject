package com.beeacademy.backend.application.support.dto;

import com.beeacademy.backend.domain.support.NotificationType;

import java.util.UUID;

public record SendNotificationCommand(
        UUID recipientId,
        String title,
        String message,
        NotificationType type,
        String actionUrl
) {
    public SendNotificationCommand {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Tiêu đề thông báo không được để trống");
        }
    }
}

