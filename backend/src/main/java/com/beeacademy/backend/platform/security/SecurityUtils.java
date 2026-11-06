package com.beeacademy.backend.platform.security;

import com.beeacademy.backend.domain.common.DomainException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Tiện ích truy xuất thông tin người dùng hiện tại từ Spring SecurityContext.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Optional<UUID> getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::userId);
    }

    public static UUID requireCurrentUserId() {
        return getCurrentUserId().orElseThrow(() ->
                new DomainException("UNAUTHORIZED", "Yêu cầu đăng nhập để thực hiện hành động này"));
    }

    public static Optional<String> getCurrentUserRole() {
        return getCurrentUser().map(UserPrincipal::role);
    }
}

