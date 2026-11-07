package com.beeacademy.backend.domain.common;

/**
 * Domain Port gửi email xác thực OTP và thông báo hệ thống.
 */
public interface MailSenderPort {

    boolean sendOtpEmail(String recipientEmail, String otpCode, int expiryMinutes);

    boolean sendNotificationEmail(String recipientEmail, String subject, String content);
}
