package com.beeacademy.backend.infrastructure.common.adapter;

import com.beeacademy.backend.domain.common.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Adapter mã hóa đối xứng AES-256-GCM cho các trường dữ liệu tài chính nhạy cảm
 * (Số tài khoản ngân hàng của giáo viên - TeacherBankAccount).
 */
@Component
public class AesEncryptionAdapter {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits chuẩn cho GCM
    private static final int GCM_TAG_LENGTH = 128; // 128 bits authentication tag

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesEncryptionAdapter(@Value("${app.security.aes-key:bee-academy-secure-aes-256-encryption-master-key!}") String keyString) {
        try {
            // Dùng SHA-256 băm keyString để luôn đảm bảo khóa có độ dài chính xác 256 bits (32 bytes)
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(keyString.getBytes(StandardCharsets.UTF_8));
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Lỗi khởi tạo thuật toán mã hóa AES-256", e);
        }
    }

    /**
     * Mã hóa chuỗi rõ thành chuỗi Base64 (đã bao gồm IV + Ciphertext + Tag).
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Đóng gói IV (12 bytes) + CipherText vào một mảng byte duy nhất
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            throw new DomainException("ENCRYPTION_FAILED", "Mã hóa dữ liệu nhạy cảm thất bại: " + e.getMessage());
        }
    }

    /**
     * Giải mã chuỗi Base64 thành chuỗi rõ ban đầu.
     */
    public String decrypt(String encryptedBase64) {
        if (encryptedBase64 == null || encryptedBase64.isBlank()) {
            return encryptedBase64;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);
            if (decoded.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Dữ liệu mã hóa không hợp lệ");
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DomainException("DECRYPTION_FAILED", "Giải mã dữ liệu nhạy cảm thất bại: " + e.getMessage());
        }
    }

    /**
     * Tạo chuỗi mặt nạ chỉ hiển thị 4 số cuối (Ví dụ: "0123456789" -> "******6789").
     */
    public String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return "";
        }
        String clean = accountNumber.trim();
        if (clean.length() <= 4) {
            return clean;
        }
        int maskLength = clean.length() - 4;
        return "*".repeat(maskLength) + clean.substring(clean.length() - 4);
    }
}

