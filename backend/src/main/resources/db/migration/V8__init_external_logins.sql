-- ============================================================================
-- V8: Khởi tạo Bảng external_logins (Quản lý liên kết OAuth Google/Facebook qua sub claim)
-- ============================================================================

CREATE TABLE IF NOT EXISTS external_logins (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID         NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    provider         VARCHAR(50)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_external_logins_provider_user UNIQUE (provider, provider_user_id)
);

CREATE INDEX IF NOT EXISTS idx_external_logins_user_id ON external_logins(user_id);
CREATE INDEX IF NOT EXISTS idx_external_logins_provider ON external_logins(provider, provider_user_id);

