-- ============================================================================
-- V10: Khởi tạo bảng Lưu trữ Hội thoại Chatbot & Khóa học mẫu bổ sung
-- ============================================================================

CREATE TABLE IF NOT EXISTS chat_conversations (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    current_intent          VARCHAR(50),
    current_filters         JSONB           NOT NULL DEFAULT '{}'::jsonb,
    last_search_course_ids  UUID[]          NOT NULL DEFAULT '{}',
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_conversations_user_id ON chat_conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_conversations_updated_at ON chat_conversations(updated_at DESC);

CREATE TABLE IF NOT EXISTS chat_messages (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id         UUID            NOT NULL REFERENCES chat_conversations(id) ON DELETE CASCADE,
    role                    VARCHAR(20)     NOT NULL, -- 'user' or 'model'
    content                 TEXT            NOT NULL,
    intent                  VARCHAR(50),
    courses_snapshot        JSONB           NOT NULL DEFAULT '[]'::jsonb,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_messages_conv_created ON chat_messages(conversation_id, created_at ASC);

-- Seed bổ sung khóa học Tiếng Anh 10 và Hóa Học 12 để phục vụ kiểm thử dữ liệu thực tế
DO $$
DECLARE
    v_teacher_thanh UUID;
    v_teacher_duc UUID;
    v_khtn_cat UUID;
    v_tienganh_cat UUID;
BEGIN
    SELECT id INTO v_teacher_thanh FROM profiles WHERE email = 'thaythanh.hoa@beeacademy.edu.vn';
    SELECT id INTO v_teacher_duc FROM profiles WHERE email = 'thayduc.toan@beeacademy.edu.vn';
    SELECT id INTO v_khtn_cat FROM categories WHERE slug = 'khoa-hoc-tu-nhien';
    SELECT id INTO v_tienganh_cat FROM categories WHERE slug = 'tieng-anh';

    IF v_teacher_thanh IS NOT NULL AND v_khtn_cat IS NOT NULL THEN
        INSERT INTO courses (
            teacher_id, category_id, title, slug, description, thumbnail_url,
            price_vnd, sale_price_vnd, grades, status, total_chapters, total_lessons,
            total_duration_sec, avg_rating, review_count, published_at
        ) VALUES (
            v_teacher_thanh, v_khtn_cat,
            'Hóa Học 12 — Chuyên Đề Este, Lipit & Hợp Chất Hữu Cơ Chứa Nitơ',
            'hoa-hoc-12-chuyen-de-este-lipit-hop-chat-nito',
            'Kỹ thuật dồn chất, giải nhanh bài tập chất béo và biện luận chuỗi phản ứng Este đa chức.',
            'https://images.unsplash.com/photo-1603126857599-f6e157fa2fe6?w=800&auto=format&fit=crop&q=80',
            199000, 169000, '{12}', 'published', 2, 4, 7200, 4.90, 14, NOW()
        ) ON CONFLICT (slug) DO UPDATE SET published_at = NOW();
    END IF;

    IF v_teacher_duc IS NOT NULL AND v_tienganh_cat IS NOT NULL THEN
        INSERT INTO courses (
            teacher_id, category_id, title, slug, description, thumbnail_url,
            price_vnd, sale_price_vnd, grades, status, total_chapters, total_lessons,
            total_duration_sec, avg_rating, review_count, published_at
        ) VALUES (
            v_teacher_duc, v_tienganh_cat,
            'Tiếng Anh 10 — Làm Chủ Ngữ Pháp B1–B2 & Từ Vựng Chuyên Đề',
            'tieng-anh-10-lam-chu-ngu-phap-b1-b2-tu-vung',
            'Chương trình bám sát sách Global Success: Các thì, mệnh đề quan hệ và từ vựng theo chủ điểm.',
            'https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=800&auto=format&fit=crop&q=80',
            180000, 150000, '{10}', 'published', 2, 4, 6800, 4.80, 12, NOW()
        ) ON CONFLICT (slug) DO UPDATE SET published_at = NOW();
    END IF;
END $$;

