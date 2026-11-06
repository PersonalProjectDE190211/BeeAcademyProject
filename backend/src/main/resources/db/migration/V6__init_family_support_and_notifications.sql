-- ============================================================================
-- V6: Khởi tạo Phân hệ Cổng Phụ huynh, Khiếu nại & Thông báo hợp nhất
-- Bảng (4): parent_student_links, complaints, complaint_messages, notifications
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 22. Bảng parent_student_links (Liên kết Phụ huynh - Học sinh - UC24..UC29)
-- Mọi truy cập xem điểm con hoặc thay đổi trạng thái được ghi vào bảng audit_logs
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS parent_student_links (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id               UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    student_id              UUID            REFERENCES profiles(id) ON DELETE CASCADE,
    student_email           VARCHAR(255)    NOT NULL,
    invite_token            VARCHAR(128)    NOT NULL UNIQUE,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'pending',
    consent_granted         BOOLEAN         NOT NULL DEFAULT FALSE,
    invite_expires_at       TIMESTAMPTZ     NOT NULL,
    linked_at               TIMESTAMPTZ,
    unlinked_at             TIMESTAMPTZ,
    unlinked_by             UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    unlink_reason           TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_parent_links_status
        CHECK (status IN ('pending', 'active', 'rejected', 'revoked'))
);

CREATE INDEX IF NOT EXISTS idx_parent_links_parent_status
    ON parent_student_links(parent_id, status);
CREATE INDEX IF NOT EXISTS idx_parent_links_student_status
    ON parent_student_links(student_id, status);

-- ----------------------------------------------------------------------------
-- 23. Bảng complaints (Khiếu nại & Hỗ trợ người dùng - UC12, UC42)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS complaints (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id               UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    sender_role             VARCHAR(20)     NOT NULL,
    title                   VARCHAR(200)    NOT NULL,
    category                VARCHAR(30)     NOT NULL DEFAULT 'other',
    priority                VARCHAR(15)     NOT NULL DEFAULT 'medium',
    status                  VARCHAR(20)     NOT NULL DEFAULT 'open',
    resolved_by_admin       UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    resolution_summary      TEXT,
    resolved_at             TIMESTAMPTZ,
    last_activity_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_complaints_category
        CHECK (category IN ('payment', 'course_content', 'teacher_conduct', 'technical', 'account', 'other')),
    CONSTRAINT chk_complaints_priority
        CHECK (priority IN ('low', 'medium', 'high', 'urgent')),
    CONSTRAINT chk_complaints_status
        CHECK (status IN ('open', 'in_progress', 'resolved', 'closed'))
);

CREATE INDEX IF NOT EXISTS idx_complaints_sender_id
    ON complaints(sender_id, last_activity_at DESC);
CREATE INDEX IF NOT EXISTS idx_complaints_status_activity
    ON complaints(status, priority, last_activity_at DESC);

-- ----------------------------------------------------------------------------
-- 24. Bảng complaint_messages (Tin nhắn hội thoại trong Khiếu nại - Chống Race Condition)
-- Đính kèm ảnh minh chứng lưu trong cột attachments JSONB của từng tin nhắn
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS complaint_messages (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_id            UUID            NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
    author_id               UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    author_role             VARCHAR(20)     NOT NULL,
    content                 TEXT            NOT NULL,
    attachments             JSONB           NOT NULL DEFAULT '[]'::jsonb,
    is_read                 BOOLEAN         NOT NULL DEFAULT FALSE,
    read_at                 TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_complaint_messages_attachments_array
        CHECK (jsonb_typeof(attachments) = 'array')
);

CREATE INDEX IF NOT EXISTS idx_complaint_messages_complaint_id
    ON complaint_messages(complaint_id, created_at ASC);

-- ----------------------------------------------------------------------------
-- 25. Bảng notifications (Thông báo hợp nhất cho Học sinh, Phụ huynh, Giáo viên & Admin - UC44)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id            UUID            REFERENCES profiles(id) ON DELETE CASCADE,
    target_role             VARCHAR(20),    -- Dùng khi gửi thông báo cho toàn bộ 'admin' hoặc broadcast
    title                   VARCHAR(255)    NOT NULL,
    message                 TEXT            NOT NULL,
    type                    VARCHAR(30)     NOT NULL DEFAULT 'general',
    action_url              VARCHAR(500),
    is_read                 BOOLEAN         NOT NULL DEFAULT FALSE,
    read_at                 TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_notifications_type
        CHECK (type IN (
            'system_alert',
            'course_update',
            'exam_result',
            'payment_success',
            'complaint_update',
            'parent_link_request',
            'general'
        ))
);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_unread
    ON notifications(recipient_id, is_read, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_target_role
    ON notifications(target_role, is_read, created_at DESC)
    WHERE recipient_id IS NULL;
