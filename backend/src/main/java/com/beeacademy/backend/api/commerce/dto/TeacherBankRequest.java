package com.beeacademy.backend.api.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TeacherBankRequest(
        @NotBlank(message = "Tên ngân hàng không được để trống")
        String bankName,

        @NotBlank(message = "Số tài khoản ngân hàng không được để trống")
        @Pattern(regexp = "^[0-9A-Za-z]{6,30}$", message = "Số tài khoản ngân hàng không hợp lệ (từ 6 đến 30 ký tự)")
        String accountNumber,

        @NotBlank(message = "Tên chủ tài khoản không được để trống")
        String accountHolder,

        String branch
) {}

