-- ============================================================================
-- V4: Khởi tạo Phân hệ Ngân hàng Câu hỏi, Quiz, Kỳ thi & Bài tập
-- Bảng (4): questions, assessments, assessment_attempts, exam_retake_requests
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 14. Bảng questions (Ngân hàng câu hỏi hỗ trợ LaTeX & Hình ảnh - UC33)
-- Cột choices JSONB lưu các lựa chọn với ID ổn định (A/B/C/D):
-- [{ "id": "A", "content": "...", "imageUrl": null, "isCorrect": true, "position": 1 }]
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS questions (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    teacher_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    category_id             UUID            NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    chapter_id              UUID            REFERENCES chapters(id) ON DELETE SET NULL,
    content                 TEXT            NOT NULL,
    image_url               VARCHAR(1000),
    explanation             TEXT,
    difficulty              VARCHAR(15)     NOT NULL DEFAULT 'medium',
    type                    VARCHAR(25)     NOT NULL DEFAULT 'multiple_choice',
    choices                 JSONB           NOT NULL DEFAULT '[]'::jsonb,
    tags                    TEXT[]          NOT NULL DEFAULT '{}',
    status                  VARCHAR(20)     NOT NULL DEFAULT 'active',
    version                 INTEGER         NOT NULL DEFAULT 1,
    usage_count             INTEGER         NOT NULL DEFAULT 0,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_questions_difficulty
        CHECK (difficulty IN ('easy', 'medium', 'hard')),
    CONSTRAINT chk_questions_type
        CHECK (type IN ('multiple_choice', 'true_false', 'essay')),
    CONSTRAINT chk_questions_status
        CHECK (status IN ('active', 'inactive', 'archived')),
    CONSTRAINT chk_questions_choices_array
        CHECK (jsonb_typeof(choices) = 'array')
);

CREATE INDEX IF NOT EXISTS idx_questions_teacher_filter
    ON questions(teacher_id, category_id, chapter_id, difficulty, status);
CREATE INDEX IF NOT EXISTS idx_questions_chapter_difficulty
    ON questions(chapter_id, difficulty) WHERE status = 'active';
CREATE INDEX IF NOT EXISTS idx_questions_tags_gin
    ON questions USING GIN (tags);

-- ----------------------------------------------------------------------------
-- 15. Bảng assessments (Hợp nhất Cấu hình Quiz chương, Bài thi Exam & Bài tập Assignment)
-- Cột config JSONB chứa cấu hình riêng theo type (easyCount/mediumCount/hardCount/shuffle hoặc instructions/dueAt)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS assessments (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id               UUID            NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    chapter_id              UUID            REFERENCES chapters(id) ON DELETE CASCADE,
    teacher_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    type                    VARCHAR(20)     NOT NULL,
    title                   VARCHAR(255)    NOT NULL,
    description             TEXT,
    time_limit_minutes      INTEGER,
    passing_score           NUMERIC(5,2)    NOT NULL DEFAULT 60.00,
    max_attempts            INTEGER         NOT NULL DEFAULT 1,
    config                  JSONB           NOT NULL DEFAULT '{}'::jsonb,
    questions_snapshot      JSONB           NOT NULL DEFAULT '[]'::jsonb,
    active                  BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_assessments_type
        CHECK (type IN ('QUIZ', 'EXAM', 'ASSIGNMENT')),
    CONSTRAINT chk_assessments_passing_score
        CHECK (passing_score BETWEEN 0.00 AND 100.00),
    CONSTRAINT chk_assessments_max_attempts
        CHECK (max_attempts >= 1),
    CONSTRAINT chk_assessments_config_object
        CHECK (jsonb_typeof(config) = 'object'),
    CONSTRAINT chk_assessments_quiz_requires_chapter
        CHECK (type <> 'QUIZ' OR chapter_id IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_assessments_course_type  ON assessments(course_id, type);
CREATE INDEX IF NOT EXISTS idx_assessments_chapter_type ON assessments(chapter_id, type);

-- ----------------------------------------------------------------------------
-- 16. Bảng assessment_attempts (Lượt làm bài, Nộp bài, Chấm điểm & Giám sát quy chế)
-- Mọi thay đổi điểm số của GV/AI được ghi thêm vào bảng audit_logs (entity_type = 'ASSESSMENT_ATTEMPT_GRADE')
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS assessment_attempts (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id           UUID            NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    student_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    attempt_number          INTEGER         NOT NULL DEFAULT 1,
    status                  VARCHAR(25)     NOT NULL DEFAULT 'in_progress',
    questions_snapshot      JSONB           NOT NULL DEFAULT '[]'::jsonb,
    answers                 JSONB           NOT NULL DEFAULT '{}'::jsonb,
    score                   NUMERIC(5,2),
    passed                  BOOLEAN,
    grader_id               UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    grading_feedback        JSONB           NOT NULL DEFAULT '{}'::jsonb,
    integrity_logs          JSONB           NOT NULL DEFAULT '[]'::jsonb,
    violation_count         INTEGER         NOT NULL DEFAULT 0,
    started_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    submitted_at            TIMESTAMPTZ,
    graded_at               TIMESTAMPTZ,

    CONSTRAINT chk_assessment_attempts_status
        CHECK (status IN ('in_progress', 'submitted', 'graded', 'invalidated')),
    CONSTRAINT chk_assessment_attempts_score
        CHECK (score IS NULL OR (score BETWEEN 0.00 AND 100.00)),
    CONSTRAINT chk_assessment_attempts_integrity_array
        CHECK (jsonb_typeof(integrity_logs) = 'array')
);

CREATE INDEX IF NOT EXISTS idx_assessment_attempts_student_assessment
    ON assessment_attempts(student_id, assessment_id, attempt_number DESC);
CREATE INDEX IF NOT EXISTS idx_assessment_attempts_assessment_status
    ON assessment_attempts(assessment_id, status);

-- ----------------------------------------------------------------------------
-- 17. Bảng exam_retake_requests (Yêu cầu xin thi lại - Tối đa 5 lần & Cooldown 12h - UC19)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS exam_retake_requests (
    id                      UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id           UUID            NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    student_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    teacher_id              UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    request_number          INTEGER         NOT NULL DEFAULT 1,
    reason                  TEXT            NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'pending',
    teacher_Note            TEXT,
    cooldown_until          TIMESTAMPTZ,
    expires_at              TIMESTAMPTZ     NOT NULL,
    responded_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_retake_requests_limit
        CHECK (request_number BETWEEN 1 AND 5),
    CONSTRAINT chk_retake_requests_status
        CHECK (status IN ('pending', 'approved', 'rejected', 'expired'))
);

CREATE INDEX IF NOT EXISTS idx_retake_requests_student_exam
    ON exam_retake_requests(student_id, assessment_id, request_number DESC);
CREATE INDEX IF NOT EXISTS idx_retake_requests_teacher_status
    ON exam_retake_requests(teacher_id, status);
