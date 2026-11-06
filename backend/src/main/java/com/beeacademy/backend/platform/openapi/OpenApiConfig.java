package com.beeacademy.backend.platform.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Swagger / OpenAPI 3.0 cho hệ thống BeeAcademy (tương đương Swagger UI trong .NET).
 * Cung cấp giao diện Web trực quan tại: http://localhost:8080/swagger-ui.html
 * Hỗ trợ nút Authorize 🔒 để dán Bearer JWT Token kiểm thử toàn bộ API bảo mật.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "BeeAcademy LMS REST API Documentation",
                version = "v1.0",
                description = "Tài liệu và giao diện kiểm thử trực tiếp API hệ thống BeeAcademy - Clean Architecture & Native Auth",
                contact = @Contact(name = "BeeAcademy Engineering Team")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Máy chủ Phát triển Cục bộ (Local)")
        },
        security = {
                @SecurityRequirement(name = "BearerAuth")
        }
)
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Nhập Access Token JWT (không cần gõ tiền tố 'Bearer ')"
)
public class OpenApiConfig {
}

