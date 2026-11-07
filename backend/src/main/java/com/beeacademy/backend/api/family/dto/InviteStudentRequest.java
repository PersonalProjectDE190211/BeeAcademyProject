package com.beeacademy.backend.api.family.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviteStudentRequest(
        @NotBlank(message = "Email học sinh không được để trống")
        @Email(message = "Email học sinh không đúng định dạng")
        String studentEmail
) {}

