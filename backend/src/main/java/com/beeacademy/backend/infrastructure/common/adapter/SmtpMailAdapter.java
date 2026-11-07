package com.beeacademy.backend.infrastructure.common.adapter;

import com.beeacademy.backend.domain.common.MailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Adapter gửi email qua giao thức SMTP (Gmail / SendGrid).
 * Tự động chuyển hướng ghi log an toàn nếu SMTP chưa được cấu hình credentials.
 */
@Component
public class SmtpMailAdapter implements MailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailAdapter.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public SmtpMailAdapter(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendOtpEmail(String recipientEmail, String otpCode, int expiryMinutes) {
        String subject = "[BeeAcademy] Mã xác thực OTP tài khoản";
        String content = String.format(
                "Xin chào,\n\nMã xác thực OTP của bạn là: %s\nMã có hiệu lực trong vòng %d phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\nTrân trọng,\nĐội ngũ BeeAcademy",
                otpCode, expiryMinutes
        );
        return sendEmail(recipientEmail, subject, content);
    }

    public boolean sendNotificationEmail(String recipientEmail, String subject, String content) {
        return sendEmail(recipientEmail, subject, content);
    }

    private boolean sendEmail(String recipientEmail, String subject, String content) {
        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            log.info("[MOCK EMAIL] Gửi đến: {} | Tiêu đề: {}\nNội dung:\n{}", recipientEmail, subject, content);
            return true;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Đã gửi email thành công tới {}", recipientEmail);
            return true;
        } catch (Exception e) {
            log.error("Gửi email tới {} thất bại: {}", recipientEmail, e.getMessage());
            return false;
        }
    }
}

