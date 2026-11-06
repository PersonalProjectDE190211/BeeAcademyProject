-- ============================================================================
-- V2: Khởi tạo Phân hệ Khóa học, Chương trình giảng dạy & Đánh giá
-- Bảng (5): categories, courses, chapters, lessons, course_reviews
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 5. Bảng categories (Danh mục môn học - Toán, Tin học, Tiếng Anh...)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name                    VARCHAR(100)    NOT NULL,
    slug                    VARCHAR(120)    NOT NULL UNIQUE,
    description             TEXT,
    icon_name               VARCHAR(50),
    display_order           INTEGER         NOT NULL DEFAULT 0,
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------
-- 6. Bảng courses (Khóa học - UC06..UC08, UC30..UC32, UC41)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS courses (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE RESTRICT,
    category_id             UUID            NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    title                   VARCHAR(255)    NOT NULL,
    slug                    VARCHAR(280)    NOT NULL UNIQUE,
    description             TEXT,
    thumbnail_url           VARCHAR(1000),
    price_vnd               INTEGER         NOT NULL DEFAULT 0,
    sale_price_vnd          INTEGER,
    grades                  INTEGER[]       NOT NULL DEFAULT '{}',
    status                  VARCHAR(25)     NOT NULL DEFAULT 'draft',
    total_chapters          INTEGER         NOT NULL DEFAULT 0,
    total_lessons           INTEGER         NOT NULL DEFAULT 0,
    total_duration_sec      INTEGER         NOT NULL DEFAULT 0,
    avg_rating              NUMERIC(3,2)    NOT NULL DEFAULT 0.00,
    review_count            INTEGER         NOT NULL DEFAULT 0,
    submitted_at            TIMESTAMPTZ,
    published_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_courses_status
        CHECK (status IN ('draft', 'pending_review', 'published', 'rejected', 'needs_revision', 'archived')),
    CONSTRAINT chk_courses_price
        CHECK (price_vnd >= 0 AND (sale_price_vnd IS NULL OR (sale_price_vnd >= 0 AND sale_price_vnd <= price_vnd)))
);

CREATE INDEX IF NOT EXISTS idx_courses_teacher_id  ON courses(teacher_id);
CREATE INDEX IF NOT EXISTS idx_courses_category_id ON courses(category_id);
CREATE INDEX IF NOT EXISTS idx_courses_status      ON courses(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_courses_grades      ON courses USING GIN (grades);

-- ----------------------------------------------------------------------------
-- 7. Bảng chapters (Chương học thuộc khóa học)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chapters (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id               UUID            NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title                   VARCHAR(255)    NOT NULL,
    description             TEXT,
    position                INTEGER         NOT NULL DEFAULT 1,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chapters_course_position ON chapters(course_id, position ASC);

-- ----------------------------------------------------------------------------
-- 8. Bảng lessons (Bài giảng video & Tài liệu đính kèm - UC08, UC13, UC15, UC31)
-- Cột documents JSONB lưu mảng: [{ "id": "uuid", "name": "...", "fileUrl": "...", "fileType": "pdf", "fileSizeBytes": 1024 }]
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lessons (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id              UUID            NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    title                   VARCHAR(255)    NOT NULL,
    description             TEXT,
    position                INTEGER         NOT NULL DEFAULT 1,
    is_free_preview         BOOLEAN         NOT NULL DEFAULT FALSE,
    video_storage_path      VARCHAR(1000),
    duration_sec            INTEGER         NOT NULL DEFAULT 0,
    documents               JSONB           NOT NULL DEFAULT '[]'::jsonb,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_lessons_duration CHECK (duration_sec >= 0),
    CONSTRAINT chk_lessons_documents_is_array CHECK (jsonb_typeof(documents) = 'array')
);

CREATE INDEX IF NOT EXISTS idx_lessons_chapter_position ON lessons(chapter_id, position ASC);

-- ----------------------------------------------------------------------------
-- 9. Bảng course_reviews (Đánh giá & Nhận xét khóa học từ Học sinh - UC22)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS course_reviews (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id               UUID            NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    student_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    rating                  INTEGER         NOT NULL,
    comment                 TEXT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_course_reviews_course_student UNIQUE (course_id, student_id),
    CONSTRAINT chk_course_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX IF NOT EXISTS idx_course_reviews_course_id ON course_reviews(course_id, created_at DESC);
