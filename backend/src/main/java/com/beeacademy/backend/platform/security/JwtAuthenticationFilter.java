package com.beeacademy.backend.platform.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Filter chặn mọi request để bóc tách Bearer JWT từ Authorization Header
 * và thiết lập SecurityContext cho phiên làm việc hiện tại.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            Optional<DecodedJWT> decodedOpt = jwtService.verifyToken(token);

            if (decodedOpt.isPresent()) {
                DecodedJWT jwt = decodedOpt.get();
                String userIdStr = jwt.getSubject();
                String role = jwt.getClaim("role").asString();
                String email = jwt.getClaim("email").asString();

                if (userIdStr != null && role != null) {
                    try {
                        UUID userId = UUID.fromString(userIdStr);
                        List<SimpleGrantedAuthority> authorities = List.of(
                                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
                        );

                        UserPrincipal principal = new UserPrincipal(userId, email, role);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(principal, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } catch (IllegalArgumentException ignored) {
                        // UUID không hợp lệ -> bỏ qua authentication
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

