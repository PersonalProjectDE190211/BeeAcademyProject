-- ============================================================================
-- V1: Khởi tạo Phân hệ Xác thực (Native Auth), Hệ thống & Kiểm toán tập trung
-- Bảng (4): profiles, refresh_tokens, system_settings, audit_logs
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ----------------------------------------------------------------------------
-- 1. Bảng profiles (Tài khoản & Hồ sơ người dùng - UC01..UC05, UC39, UC40)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS profiles (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    email                   VARCHAR(255)    NOT NULL UNIQUE,
    password_hash           VARCHAR(255)    NOT NULL,
    full_name               VARCHAR(150)    NOT NULL,
    avatar_url              VARCHAR(1000),
    phone                   VARCHAR(30),
    bio                     TEXT,
    role                    VARCHAR(20)     NOT NULL DEFAULT 'student',
    grade                   INTEGER,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'active',
    teacher_approval_status VARCHAR(20)     NOT NULL DEFAULT 'none',
    email_verified          BOOLEAN         NOT NULL DEFAULT FALSE,
    failed_login_attempts   INTEGER         NOT NULL DEFAULT 0,
    locked_until            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_profiles_role
        CHECK (role IN ('student', 'parent', 'teacher', 'admin')),
    CONSTRAINT chk_profiles_status
        CHECK (status IN ('active', 'locked', 'suspended')),
    CONSTRAINT chk_profiles_teacher_approval
        CHECK (teacher_approval_status IN ('none', 'pending', 'approved', 'rejected')),
    CONSTRAINT chk_profiles_grade
        CHECK (grade IS NULL OR (grade BETWEEN 1 AND 12))
);

CREATE INDEX IF NOT EXISTS idx_profiles_role   ON profiles(role);
CREATE INDEX IF NOT EXISTS idx_profiles_status ON profiles(status);

-- ----------------------------------------------------------------------------
-- 2. Bảng refresh_tokens (Native Auth Refresh Token Rotation & Theft Detection)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    token_hash              VARCHAR(128)    NOT NULL UNIQUE,
    family_id               UUID            NOT NULL,
    replaced_by_token_hash  VARCHAR(128),
    device_info             VARCHAR(500),
    ip_address              VARCHAR(64),
    expires_at              TIMESTAMPTZ     NOT NULL,
    revoked                 BOOLEAN         NOT NULL DEFAULT FALSE,
    revoked_at              TIMESTAMPTZ,
    revoke_reason           VARCHAR(255),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id   ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_family_id ON refresh_tokens(family_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires   ON refresh_tokens(expires_at);

-- ----------------------------------------------------------------------------
-- 3. Bảng system_settings (Cấu hình hệ thống & Chế độ bảo trì)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS system_settings (
    key                     VARCHAR(100)    PRIMARY KEY,
    value                   JSONB           NOT NULL DEFAULT '{}'::jsonb,
    description             VARCHAR(500),
    updated_by              UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- 4. Bảng audit_logs (Nhật ký kiểm toán tập trung bất biến - Append-Only)
-- Phủ: Duyệt khóa học, Đổi STK ngân hàng, Chấm/Sửa điểm, Phụ huynh xem điểm con
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type             VARCHAR(50)     NOT NULL,
    entity_id               UUID            NOT NULL,
    action                  VARCHAR(50)     NOT NULL,
    actor_id                UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    actor_role              VARCHAR(20),
    reason                  TEXT,
    diff                    JSONB           NOT NULL DEFAULT '{}'::jsonb,
    metadata                JSONB           NOT NULL DEFAULT '{}'::jsonb,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_audit_logs_entity_type
        CHECK (entity_type IN (
            'COURSE_APPROVAL',
            'TEACHER_BANK_ACCOUNT',
            'ASSESSMENT_ATTEMPT_GRADE',
            'PARENT_STUDENT_LINK',
            'PARENT_PROGRESS_ACCESS',
            'QUESTION_BANK',
            'USER_ACCOUNT',
            'SYSTEM_SETTING'
        ))
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_entity
    ON audit_logs(entity_type, entity_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_actor
    ON audit_logs(actor_id, action, created_at DESC);
