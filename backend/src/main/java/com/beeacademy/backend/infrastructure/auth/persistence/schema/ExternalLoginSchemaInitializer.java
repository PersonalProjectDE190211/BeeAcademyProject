package com.beeacademy.backend.infrastructure.auth.persistence.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Đảm bảo bảng 'external_logins' tồn tại trong cơ sở dữ liệu Supabase Postgres.
 * Khởi tạo an toàn (idempotent qua CREATE TABLE IF NOT EXISTS) trước khi xử lý request đầu tiên.
 */
@Component
@Order(1)
public class ExternalLoginSchemaInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ExternalLoginSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public ExternalLoginSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS external_logins (
                    id               UUID         PRIMARY KEY,
                    user_id          UUID         NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
                    provider         VARCHAR(50)  NOT NULL,
                    provider_user_id VARCHAR(255) NOT NULL,
                    email            VARCHAR(255) NOT NULL,
                    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                    CONSTRAINT uq_external_logins_provider_user UNIQUE (provider, provider_user_id)
                );
                CREATE INDEX IF NOT EXISTS idx_external_logins_user_id ON external_logins(user_id);
            """);
            log.info("Xác thực schema bảng 'external_logins' thành công");
        } catch (Exception e) {
            log.warn("Lưu ý khi khởi tạo schema external_logins: {}", e.getMessage());
        }
    }
}

