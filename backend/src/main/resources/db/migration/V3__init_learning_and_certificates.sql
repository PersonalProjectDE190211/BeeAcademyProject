-- ============================================================================
-- V3: Khởi tạo Phân hệ Không gian Học tập, Tiến độ, Hỏi đáp & Chứng chỉ
-- Bảng (4): enrollments, lesson_progress, lesson_discussions, certificates
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 10. Bảng enrollments (Ghi danh khóa học & Tiến độ tổng thể - UC13, UC14)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS enrollments (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    course_id               UUID            NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    completed_lessons       INTEGER         NOT NULL DEFAULT 0,
    total_lessons           INTEGER         NOT NULL DEFAULT 0,
    progress_percent        NUMERIC(5,2)    NOT NULL DEFAULT 0.00,
    final_exam_passed       BOOLEAN         NOT NULL DEFAULT FALSE,
    completed_at            TIMESTAMPTZ,
    enrolled_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_enrollments_student_course UNIQUE (student_id, course_id),
    CONSTRAINT chk_enrollments_progress CHECK (progress_percent BETWEEN 0.00 AND 100.00)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_student_id ON enrollments(student_id, enrolled_at DESC);
CREATE INDEX IF NOT EXISTS idx_enrollments_course_id  ON enrollments(course_id);

-- ----------------------------------------------------------------------------
-- 11. Bảng lesson_progress (Tiến độ xem video 80%, Ghi chú & Tải tài liệu - UC14, UC15)
-- Gộp thống nhất: watch_segments JSONB (chống tua), notes JSONB, downloaded_doc_ids JSONB
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lesson_progress (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    lesson_id               UUID            NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    course_id               UUID            NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    watched_seconds         INTEGER         NOT NULL DEFAULT 0,
    last_position_sec       INTEGER         NOT NULL DEFAULT 0,
    watch_segments          JSONB           NOT NULL DEFAULT '[]'::jsonb,
    notes                   JSONB           NOT NULL DEFAULT '[]'::jsonb,
    downloaded_doc_ids      JSONB           NOT NULL DEFAULT '[]'::jsonb,
    is_completed            BOOLEAN         NOT NULL DEFAULT FALSE,
    completed_at            TIMESTAMPTZ,
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_lesson_progress_student_lesson UNIQUE (student_id, lesson_id),
    CONSTRAINT chk_lesson_progress_watched CHECK (watched_seconds >= 0 AND last_position_sec >= 0),
    CONSTRAINT chk_lesson_progress_segments_array CHECK (jsonb_typeof(watch_segments) = 'array'),
    CONSTRAINT chk_lesson_progress_notes_array CHECK (jsonb_typeof(notes) = 'array'),
    CONSTRAINT chk_lesson_progress_docs_array CHECK (jsonb_typeof(downloaded_doc_ids) = 'array')
);

CREATE INDEX IF NOT EXISTS idx_lesson_progress_student_course
    ON lesson_progress(student_id, course_id);

-- ----------------------------------------------------------------------------
-- 12. Bảng lesson_discussions (Hỏi đáp Q&A dưới bài giảng giữa HS & GV - UC21, UC36)
-- Tự tham chiếu qua parent_id: NULL = câu hỏi gốc, có giá trị = câu trả lời
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lesson_discussions (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id               UUID            NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    course_id               UUID            NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    author_id               UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    author_role             VARCHAR(20)     NOT NULL,
    parent_id               UUID            REFERENCES lesson_discussions(id) ON DELETE CASCADE,
    content                 TEXT            NOT NULL,
    is_resolved             BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_lesson_discussions_lesson_id ON lesson_discussions(lesson_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_lesson_discussions_course_id ON lesson_discussions(course_id, is_resolved);
CREATE INDEX IF NOT EXISTS idx_lesson_discussions_parent_id ON lesson_discussions(parent_id);

-- ----------------------------------------------------------------------------
-- 13. Bảng certificates (Chứng chỉ hoàn thành khóa học PDF + QR - UC20)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS certificates (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id           UUID            NOT NULL UNIQUE REFERENCES enrollments(id) ON DELETE CASCADE,
    student_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    course_id               UUID            NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    serial_number           VARCHAR(64)     NOT NULL UNIQUE,
    student_name_snapshot   VARCHAR(150)    NOT NULL,
    course_title_snapshot   VARCHAR(255)    NOT NULL,
    final_exam_score        NUMERIC(5,2)    NOT NULL,
    pdf_url                 VARCHAR(1000),
    verification_qr_url     VARCHAR(1000),
    revoked                 BOOLEAN         NOT NULL DEFAULT FALSE,
    issued_at               TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_certificates_student_id ON certificates(student_id);
