package com.beeacademy.backend.application.support.dto;

import com.beeacademy.backend.domain.support.ComplaintCategory;
import com.beeacademy.backend.domain.support.ComplaintPriority;

import java.util.UUID;

public record CreateComplaintCommand(
        UUID senderId,
        String senderRole,
        String title,
        ComplaintCategory category,
        ComplaintPriority priority,
        String messageContent
) {
    public CreateComplaintCommand {
        if (senderId == null) {
            throw new IllegalArgumentException("Sender ID không được để trống");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Tiêu đề khiếu nại không được để trống");
        }
        if (messageContent == null || messageContent.isBlank()) {
            throw new IllegalArgumentException("Nội dung khiếu nại không được để trống");
        }
    }
}

