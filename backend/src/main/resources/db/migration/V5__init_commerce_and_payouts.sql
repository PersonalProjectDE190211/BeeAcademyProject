-- ============================================================================
-- V5: Khởi tạo Phân hệ Thương mại, Đơn hàng PayOS & Đối soát Doanh thu Giáo viên
-- Bảng (4): teacher_bank_accounts, payout_periods, orders, order_items
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 18. Bảng teacher_bank_accounts (Tài khoản ngân hàng nhận tiền của Giáo viên)
-- Bảo mật: account_number_encrypted lưu bản mã AES-256-GCM; lịch sử đổi STK ghi vào audit_logs
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS teacher_bank_accounts (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id              UUID            NOT NULL UNIQUE REFERENCES profiles(id) ON DELETE CASCADE,
    bank_name               VARCHAR(100)    NOT NULL,
    account_number_encrypted VARCHAR(500)   NOT NULL,
    account_number_masked   VARCHAR(30)     NOT NULL,
    account_holder          VARCHAR(150)    NOT NULL,
    branch                  VARCHAR(150),
    verify_status           VARCHAR(20)     NOT NULL DEFAULT 'pending',
    otp_verified            BOOLEAN         NOT NULL DEFAULT FALSE,
    verified_by_admin       UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    verified_at             TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_teacher_bank_verify_status
        CHECK (verify_status IN ('pending', 'verified', 'rejected'))
);

CREATE INDEX IF NOT EXISTS idx_teacher_bank_verify_status
    ON teacher_bank_accounts(verify_status);

-- ----------------------------------------------------------------------------
-- 19. Bảng payout_periods (Kỳ quyết toán chuyển khoản doanh thu tháng cho GV - UC37, UC43)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payout_periods (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE RESTRICT,
    month_year              VARCHAR(7)      NOT NULL, -- Định dạng "YYYY-MM"
    status                  VARCHAR(20)     NOT NULL DEFAULT 'pending',
    total_gross_vnd         INTEGER         NOT NULL DEFAULT 0,
    total_platform_fee_vnd  INTEGER         NOT NULL DEFAULT 0,
    total_teacher_vnd       INTEGER         NOT NULL DEFAULT 0,
    transaction_count       INTEGER         NOT NULL DEFAULT 0,
    bank_snapshot           JSONB           NOT NULL DEFAULT '{}'::jsonb,
    paid_by_admin           UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    transfer_ref            VARCHAR(100),
    transfer_content        TEXT,
    unc_attachment_url      VARCHAR(1000),
    paid_at                 TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_payout_periods_teacher_month UNIQUE (teacher_id, month_year),
    CONSTRAINT chk_payout_periods_status
        CHECK (status IN ('pending', 'processing', 'paid', 'rejected')),
    CONSTRAINT chk_payout_periods_ledger_balance
        CHECK (total_gross_vnd = total_platform_fee_vnd + total_teacher_vnd)
);

CREATE INDEX IF NOT EXISTS idx_payout_periods_teacher_month
    ON payout_periods(teacher_id, month_year DESC);
CREATE INDEX IF NOT EXISTS idx_payout_periods_status
    ON payout_periods(status);

-- ----------------------------------------------------------------------------
-- 20. Bảng orders (Đơn hàng mua khóa học - Hết hạn sau 15 phút - UC09, UC10, UC11)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE RESTRICT,
    payment_ref             VARCHAR(50)     NOT NULL UNIQUE,
    order_code              BIGINT          UNIQUE,
    payment_link_id         VARCHAR(100),
    payment_method          VARCHAR(20)     NOT NULL DEFAULT 'PAYOS',
    status                  VARCHAR(20)     NOT NULL DEFAULT 'pending',
    total_amount_vnd        INTEGER         NOT NULL,
    discount_amount_vnd     INTEGER         NOT NULL DEFAULT 0,
    expires_at              TIMESTAMPTZ     NOT NULL,
    paid_at                 TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_orders_status
        CHECK (status IN ('pending', 'paid', 'cancelled', 'expired', 'refunded')),
    CONSTRAINT chk_orders_amounts
        CHECK (total_amount_vnd >= 0 AND discount_amount_vnd >= 0)
);

CREATE INDEX IF NOT EXISTS idx_orders_student_id  ON orders(student_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_status_exp  ON orders(status, expires_at);

-- ----------------------------------------------------------------------------
-- 21. Bảng order_items (Chi tiết khóa học trong đơn & Phân chia doanh thu 80/20)
-- Tích hợp trực tiếp tỷ lệ phí tại thời điểm mua (platform_fee_rate) và trạng thái hoàn tiền (item_status)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID            NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    course_id               UUID            NOT NULL REFERENCES courses(id) ON DELETE RESTRICT,
    teacher_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE RESTRICT,
    course_title_snapshot   VARCHAR(255)    NOT NULL,
    price_at_purchase       INTEGER         NOT NULL,
    platform_fee_rate       NUMERIC(5,2)    NOT NULL DEFAULT 20.00,
    platform_fee_vnd        INTEGER         NOT NULL DEFAULT 0,
    teacher_amount_vnd      INTEGER         NOT NULL DEFAULT 0,
    item_status             VARCHAR(20)     NOT NULL DEFAULT 'pending',
    payout_period_id        UUID            REFERENCES payout_periods(id) ON DELETE SET NULL,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    refunded_at             TIMESTAMPTZ,

    CONSTRAINT uq_order_items_order_course UNIQUE (order_id, course_id),
    CONSTRAINT chk_order_items_status
        CHECK (item_status IN ('pending', 'paid', 'refunded')),
    CONSTRAINT chk_order_items_fee_rate
        CHECK (platform_fee_rate BETWEEN 0.00 AND 100.00),
    CONSTRAINT chk_order_items_ledger_invariant
        CHECK (price_at_purchase = platform_fee_vnd + teacher_amount_vnd)
);

CREATE INDEX IF NOT EXISTS idx_order_items_teacher_status
    ON order_items(teacher_id, item_status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_order_items_payout_period
    ON order_items(payout_period_id);
