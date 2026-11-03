package com.beeacademy.backend.domain.auth;

import com.beeacademy.backend.domain.common.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Tầng Domain Auth (RefreshToken Entity & Rotation Policy)")
class RefreshTokenDomainTest {

    private static final String VALID_HASH_1 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final String VALID_HASH_2 = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @Test
    @DisplayName("Cấp phát Refresh Token mới với băm SHA-256 (64 ký tự) và thời gian sống hợp lệ")
    void testIssueRefreshTokenSuccess() {
        UUID userId = UUID.randomUUID();
        Duration ttl = Duration.ofDays(7);

        RefreshToken token = RefreshToken.issue(userId, VALID_HASH_1, ttl);

        assertNotNull(token.getId());
        assertEquals(userId, token.getUserId());
        assertEquals(VALID_HASH_1, token.getTokenHash());
        assertNotNull(token.getCreatedAt());
        assertNotNull(token.getExpiresAt());
        assertTrue(token.getExpiresAt().isAfter(token.getCreatedAt()));
        assertFalse(token.isRevoked());
        assertNull(token.getRevokedAt());
        assertNull(token.getReplacedByTokenHash());
        assertTrue(token.isValid(Instant.now()));
    }

    @Test
    @DisplayName("Từ chối tạo token nếu User ID null, Hash sai định dạng, hoặc TTL âm/bằng 0")
    void testIssueRefreshTokenValidation() {
        UUID userId = UUID.randomUUID();
        Duration ttl = Duration.ofDays(7);

        // User ID null
        assertThrows(DomainException.class, () -> RefreshToken.issue(null, VALID_HASH_1, ttl));

        // Hash null, rỗng hoặc không đủ 64 ký tự hex
        assertThrows(DomainException.class, () -> RefreshToken.issue(userId, null, ttl));
        assertThrows(DomainException.class, () -> RefreshToken.issue(userId, "abc123short", ttl));
        assertThrows(DomainException.class, () -> RefreshToken.issue(userId, VALID_HASH_1 + "extra", ttl));

        // TTL null, negative hoặc 0
        assertThrows(DomainException.class, () -> RefreshToken.issue(userId, VALID_HASH_1, null));
        assertThrows(DomainException.class, () -> RefreshToken.issue(userId, VALID_HASH_1, Duration.ofSeconds(0)));
        assertThrows(DomainException.class, () -> RefreshToken.issue(userId, VALID_HASH_1, Duration.ofMinutes(-5)));
    }

    @Test
    @DisplayName("Kiểm tra trạng thái hết hạn: isExpired trả về true khi thời điểm hiện tại vượt quá expiresAt")
    void testExpirationCheck() {
        UUID userId = UUID.randomUUID();
        RefreshToken token = RefreshToken.issue(userId, VALID_HASH_1, Duration.ofMinutes(10));

        Instant beforeExpiry = token.getExpiresAt().minusSeconds(10);
        Instant afterExpiry = token.getExpiresAt().plusSeconds(10);

        assertFalse(token.isExpired(beforeExpiry));
        assertTrue(token.isValid(beforeExpiry));

        assertTrue(token.isExpired(afterExpiry));
        assertFalse(token.isValid(afterExpiry));
    }

    @Test
    @DisplayName("Thực hiện xoay vòng token hợp lệ (Refresh Token Rotation): Revoke token cũ và lưu token mới")
    void testRotateTokenSuccess() {
        UUID userId = UUID.randomUUID();
        RefreshToken token = RefreshToken.issue(userId, VALID_HASH_1, Duration.ofDays(7));
        Instant rotateTime = Instant.now().plusSeconds(60);

        token.rotate(VALID_HASH_2, rotateTime);

        assertTrue(token.isRevoked());
        assertEquals(rotateTime, token.getRevokedAt());
        assertEquals(VALID_HASH_2, token.getReplacedByTokenHash());
        assertFalse(token.isValid(rotateTime));
    }

    @Test
    @DisplayName("Bảo mật Token Theft: Từ chối xoay vòng nếu token đã bị revoke trước đó hoặc đã hết hạn")
    void testRotateTokenFailsIfAlreadyRevokedOrExpired() {
        UUID userId = UUID.randomUUID();
        RefreshToken token = RefreshToken.issue(userId, VALID_HASH_1, Duration.ofMinutes(10));
        Instant now = Instant.now();

        // Xoay vòng lần 1 -> Thành công
        token.rotate(VALID_HASH_2, now);

        // Kẻ tấn công hoặc client gửi lại token cũ đã bị thu hồi -> Ném exception TOKEN_ALREADY_REVOKED
        DomainException ex = assertThrows(DomainException.class, () -> token.rotate(VALID_HASH_1, now.plusSeconds(5)));
        assertEquals("TOKEN_ALREADY_REVOKED", ex.getErrorCode());

        // Token đã hết hạn không được phép xoay vòng
        RefreshToken expiredToken = RefreshToken.issue(userId, VALID_HASH_1, Duration.ofSeconds(5));
        Instant future = Instant.now().plusSeconds(100);
        DomainException exExpired = assertThrows(DomainException.class, () -> expiredToken.rotate(VALID_HASH_2, future));
        assertEquals("TOKEN_EXPIRED", exExpired.getErrorCode());
    }

    @Test
    @DisplayName("Người dùng đăng xuất: Thu hồi trực tiếp token (revoke)")
    void testManualRevokeOnLogout() {
        UUID userId = UUID.randomUUID();
        RefreshToken token = RefreshToken.issue(userId, VALID_HASH_1, Duration.ofDays(7));
        Instant logoutTime = Instant.now();

        token.revoke(logoutTime);

        assertTrue(token.isRevoked());
        assertEquals(logoutTime, token.getRevokedAt());
        assertNull(token.getReplacedByTokenHash());
        assertFalse(token.isValid(logoutTime));
    }
}

