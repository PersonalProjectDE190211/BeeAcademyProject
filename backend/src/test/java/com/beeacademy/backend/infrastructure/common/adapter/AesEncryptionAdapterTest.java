package com.beeacademy.backend.infrastructure.common.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AesEncryptionAdapterTest {

    private final AesEncryptionAdapter adapter = new AesEncryptionAdapter("test-master-encryption-key-for-bank-accounts-12345");

    @Test
    @DisplayName("Mã hóa và giải mã số tài khoản ngân hàng bằng AES-256-GCM")
    void testEncryptAndDecrypt() {
        String originalAccountNumber = "0451000388299";

        String encrypted = adapter.encrypt(originalAccountNumber);
        assertThat(encrypted).isNotBlank();
        assertThat(encrypted).isNotEqualTo(originalAccountNumber);

        String decrypted = adapter.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(originalAccountNumber);
    }

    @Test
    @DisplayName("Mặt nạ hóa số tài khoản ngân hàng chỉ để lộ 4 chữ số cuối")
    void testMaskAccountNumber() {
        assertThat(adapter.maskAccountNumber("0451000388299")).isEqualTo("*********8299");
        assertThat(adapter.maskAccountNumber("1234")).isEqualTo("1234");
        assertThat(adapter.maskAccountNumber("12")).isEqualTo("12");
        assertThat(adapter.maskAccountNumber("")).isEmpty();
        assertThat(adapter.maskAccountNumber(null)).isEmpty();
    }
}

