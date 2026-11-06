package com.beeacademy.backend.application.auth.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý mã xác thực OTP (One-Time Password) lưu tạm trong bộ nhớ.
 */
@Service
public class OtpService {

    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public record OtpEntry(String code, Instant expiresAt) {
        public boolean isValid(String inputCode, Instant now) {
            return now.isBefore(expiresAt) && code.equals(inputCode);
        }
    }

    public String generateOtp(String key, int expiryMinutes) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plusSeconds(expiryMinutes * 60L);
        otpStorage.put(key.toLowerCase().trim(), new OtpEntry(code, expiresAt));
        return code;
    }

    public boolean verifyOtp(String key, String code) {
        String normalizedKey = key.toLowerCase().trim();
        OtpEntry entry = otpStorage.get(normalizedKey);
        if (entry == null) {
            return false;
        }

        boolean valid = entry.isValid(code.trim(), Instant.now());
        if (valid) {
            otpStorage.remove(normalizedKey); // Dùng 1 lần duy nhất
        }
        return valid;
    }
}

