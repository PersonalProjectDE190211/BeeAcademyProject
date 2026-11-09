package com.beeacademy.backend.domain.auth;

/**
 * Port thẩm định tính hợp lệ của Google ID Token (chữ ký số, issuer, audience, thời hạn và claims).
 */
public interface GoogleTokenVerifierPort {

    GoogleUserClaims verify(String idToken);
}

