package com.beeacademy.backend.application.family.dto;

import java.util.UUID;

public record InviteStudentCommand(
        UUID parentId,
        String studentEmail
) {
    public InviteStudentCommand {
        if (parentId == null) {
            throw new IllegalArgumentException("Parent ID không được để trống");
        }
        if (studentEmail == null || studentEmail.isBlank()) {
            throw new IllegalArgumentException("Email học sinh không được để trống");
        }
    }
}

