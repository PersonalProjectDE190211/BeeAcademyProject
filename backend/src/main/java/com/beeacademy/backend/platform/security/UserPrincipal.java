package com.beeacademy.backend.platform.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

/**
 * Principal đại diện cho người dùng đã xác thực trong SecurityContext.
 */
public record UserPrincipal(
        UUID userId,
        String email,
        String role
) implements Principal, Serializable {

    @Override
    public String getName() {
        return email != null ? email : userId.toString();
    }
}

