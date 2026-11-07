package com.beeacademy.backend.application.support.dto;

import java.util.UUID;

public record AddComplaintMessageCommand(
        UUID complaintId,
        UUID authorId,
        String authorRole,
        String content
) {
    public AddComplaintMessageCommand {
        if (complaintId == null) {
            throw new IllegalArgumentException("Complaint ID không được để trống");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("Author ID không được để trống");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được để trống");
        }
    }
}

