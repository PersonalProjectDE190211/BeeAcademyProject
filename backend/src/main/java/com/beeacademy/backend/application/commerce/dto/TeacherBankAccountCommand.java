package com.beeacademy.backend.application.commerce.dto;

public record TeacherBankAccountCommand(
        String bankName,
        String accountNumber,
        String accountHolder,
        String branch
) {
    public TeacherBankAccountCommand {
        if (bankName == null || bankName.isBlank()) {
            throw new IllegalArgumentException("Tên ngân hàng không được để trống");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Số tài khoản không được để trống");
        }
        if (accountHolder == null || accountHolder.isBlank()) {
            throw new IllegalArgumentException("Tên chủ tài khoản không được để trống");
        }
    }
}

