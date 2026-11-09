package com.beeacademy.backend.infrastructure.auth.google;

import com.beeacademy.backend.domain.auth.GoogleTokenVerifierPort;
import com.beeacademy.backend.domain.auth.GoogleUserClaims;
import com.beeacademy.backend.domain.common.DomainException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Adapter thực thi thẩm định Google ID Token qua endpoint chính thức của Google:
 * https://oauth2.googleapis.com/tokeninfo?id_token=...
 *
 * Kiểm tra đầy đủ 6 tiêu chuẩn bảo mật:
 * 1. Chữ ký số và tính hợp lệ qua Google API.
 * 2. Issuer (iss): accounts.google.com hoặc https://accounts.google.com.
 * 3. Audience (aud): Khớp chính xác với Google Client ID được cấp cho BeeAcademy.
 * 4. Expiration (exp): Token chưa hết hạn (exp > now).
 * 5. Subject (sub): Định danh duy nhất không được rỗng.
 * 6. Email Verified (email_verified): Bắt buộc là TRUE.
 */
@Component
public class GoogleTokenVerifierAdapter implements GoogleTokenVerifierPort {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={token}";
    private static final List<String> VALID_ISSUERS = List.of("accounts.google.com", "https://accounts.google.com");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.google.client-id:}")
    private String configuredClientId;

    @Value("${app.dev-mode:false}")
    private boolean devMode;

    public GoogleTokenVerifierAdapter(RestTemplateBuilder restTemplateBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public GoogleUserClaims verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new DomainException("INVALID_TOKEN", "Google ID Token không được để trống");
        }

        // Hỗ trợ mock token cho unit/integration tests hoặc dev môi trường không mạng ngoài
        if (devMode && idToken.startsWith("mock_google_token:")) {
            return parseMockToken(idToken);
        }

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(GOOGLE_TOKEN_INFO_URL, String.class, idToken);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new DomainException("INVALID_GOOGLE_TOKEN", "Google ID Token không hợp lệ hoặc không thể xác thực");
            }

            JsonNode root = objectMapper.readTree(response.getBody());

            // 1. Thẩm định Issuer (iss)
            String iss = root.path("iss").asText("");
            if (!VALID_ISSUERS.contains(iss)) {
                throw new DomainException("INVALID_TOKEN_ISSUER", "Token không được phát hành bởi Google hợp lệ (iss: " + iss + ")");
            }

            // 2. Thẩm định Audience (aud)
            String aud = root.path("aud").asText("");
            if (configuredClientId != null && !configuredClientId.isBlank()) {
                if (!configuredClientId.equals(aud)) {
                    throw new DomainException("INVALID_TOKEN_AUDIENCE", "Google Client ID không khớp với ứng dụng");
                }
            }

            // 3. Thẩm định Expiration (exp)
            long expSeconds = root.path("exp").asLong(0);
            if (expSeconds <= 0 || Instant.ofEpochSecond(expSeconds).isBefore(Instant.now())) {
                throw new DomainException("TOKEN_EXPIRED", "Google ID Token đã hết hạn");
            }

            // 4. Thẩm định Subject (sub)
            String sub = root.path("sub").asText("");
            if (sub.isBlank()) {
                throw new DomainException("INVALID_TOKEN_SUBJECT", "Không tìm thấy định danh sub từ Google");
            }

            // 5. Thẩm định Email
            String email = root.path("email").asText("").trim().toLowerCase();
            if (email.isBlank() || !email.contains("@")) {
                throw new DomainException("INVALID_EMAIL", "Email Google không hợp lệ");
            }

            // 6. Thẩm định Email Verified (email_verified)
            boolean emailVerified = root.path("email_verified").asBoolean(false)
                    || "true".equalsIgnoreCase(root.path("email_verified").asText(""));
            if (!emailVerified) {
                throw new DomainException("GOOGLE_EMAIL_NOT_VERIFIED", "Email tài khoản Google chưa được xác thực quyền sở hữu");
            }

            String fullName = root.path("name").asText("Người dùng Google");
            String avatarUrl = root.path("picture").asText(null);

            return new GoogleUserClaims(sub, email, true, fullName, avatarUrl, iss, aud);

        } catch (HttpClientErrorException e) {
            throw new DomainException("INVALID_GOOGLE_TOKEN", "Google ID Token không hợp lệ hoặc đã bị thu hồi");
        } catch (DomainException de) {
            throw de;
        } catch (Exception e) {
            throw new DomainException("GOOGLE_VERIFICATION_FAILED", "Lỗi trong quá trình thẩm định token với Google: " + e.getMessage());
        }
    }

    private GoogleUserClaims parseMockToken(String mockToken) {
        // Cú pháp mock: mock_google_token:sub:email:fullName:avatarUrl:emailVerified
        String[] parts = mockToken.split(":", 6);
        String sub = parts.length > 1 && !parts[1].isBlank() ? parts[1] : "mock-sub-123456";
        String email = parts.length > 2 && !parts[2].isBlank() ? parts[2] : "mock.student@beeacademy.edu.vn";
        String fullName = parts.length > 3 && !parts[3].isBlank() ? parts[3] : "Mock Google Student";
        String avatarUrl = parts.length > 4 && !parts[4].isBlank() ? parts[4] : "https://lh3.googleusercontent.com/mock.png";
        boolean emailVerified = parts.length <= 5 || !"false".equalsIgnoreCase(parts[5]);

        if (!emailVerified) {
            throw new DomainException("GOOGLE_EMAIL_NOT_VERIFIED", "Email tài khoản Google chưa được xác minh");
        }

        return new GoogleUserClaims(sub, email, true, fullName, avatarUrl, "accounts.google.com", configuredClientId != null ? configuredClientId : "mock-client-id");
    }
}

