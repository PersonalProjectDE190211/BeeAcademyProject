package com.beeacademy.backend.domain.media;

import java.io.Serializable;

/**
 * Dữ liệu xác thực chữ ký số (Signed Upload) cho phép Frontend tải trực tiếp file lớn lên Cloudinary
 * mà không cần trung chuyển qua server Spring Boot, tiết kiệm 100% băng thông server.
 */
public record MediaSignature(
        String signature,
        long timestamp,
        String apiKey,
        String cloudName,
        String folder
) implements Serializable {
}
