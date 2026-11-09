package com.beeacademy.backend.domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain: ExternalLogin Aggregate & Value Invariants")
class ExternalLoginDomainTest {

    @Test
    @DisplayName("Khởi tạo ExternalLogin thành công với đầy đủ thuộc tính")
    void shouldCreateExternalLoginSuccessfully() {
        UUID userId = UUID.randomUUID();
        ExternalLogin login = ExternalLogin.create(userId, "GOOGLE", "sub-123456", "student@beeacademy.edu.vn");

        assertNotNull(login.getId());
        assertEquals(userId, login.getUserId());
        assertEquals("GOOGLE", login.getProvider());
        assertEquals("sub-123456", login.getProviderUserId());
        assertEquals("student@beeacademy.edu.vn", login.getEmail());
        assertNotNull(login.getCreatedAt());
    }

    @Test
    @DisplayName("Ném lỗi khi UserId hoặc Provider hoặc Sub rỗng")
    void shouldThrowExceptionWhenRequiredFieldsAreMissing() {
        UUID userId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                ExternalLogin.create(null, "GOOGLE", "sub-123", "a@b.com"));

        assertThrows(IllegalArgumentException.class, () ->
                ExternalLogin.create(userId, "", "sub-123", "a@b.com"));

        assertThrows(IllegalArgumentException.class, () ->
                ExternalLogin.create(userId, "GOOGLE", "", "a@b.com"));
    }
}

