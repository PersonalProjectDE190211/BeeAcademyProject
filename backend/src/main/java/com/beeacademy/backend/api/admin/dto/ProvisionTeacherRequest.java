package com.beeacademy.backend.api.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProvisionTeacherRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mật khẩu tạm không được để trống")
        @Size(min = 6, message = "Mật khẩu tạm phải có ít nhất 6 ký tự")
        String temporaryPassword,

        @NotBlank(message = "Họ tên giáo viên không được để trống")
        String fullName,

        String contactNote
) {
}

