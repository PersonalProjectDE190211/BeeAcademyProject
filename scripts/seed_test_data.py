import os
import sys
import uuid
import datetime
import psycopg2
from psycopg2.extras import Json

# DB Connection Config from .env
DB_HOST = "aws-0-ap-northeast-2.pooler.supabase.com"
DB_PORT = 5432
DB_NAME = "postgres"
DB_USER = "postgres.wnsibvtgqlkqwxwulbff"
DB_PASS = "Thanhdatvv05@"

def run_seed():
    print("🚀 Đang kết nối tới Supabase Postgres...")
    conn = psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASS,
        sslmode="require"
    )
    conn.autocommit = False
    cur = conn.cursor()

    try:
        # Bắt buộc pgcrypto để sinh BCrypt hash chuẩn cho mật khẩu Bee@123456
        cur.execute('CREATE EXTENSION IF NOT EXISTS "pgcrypto";')
        
        # Tạo hàm hash mật khẩu Bee@123456
        # crypt('Bee@123456', gen_salt('bf', 12))
        
        print("👤 1. Khởi tạo/Cập nhật Tài khoản Người dùng (Profiles)...")
        # Danh sách người dùng:
        # 1. Admin: admin@beeacademy.edu.vn / Bee@123456
        # 2. Thầy Đức: thayduc.toan@beeacademy.edu.vn / Bee@123456
        # 3. Cô Phan: cophan.vatly@beeacademy.edu.vn / Bee@123456
        # 4. Thầy Thành: thaythanh.hoa@beeacademy.edu.vn / Bee@123456
        # 5. Học sinh Nam: hocsinh.nam@beeacademy.edu.vn / Bee@123456
        # 6. Học sinh Mai: hocsinh.mai@beeacademy.edu.vn / Bee@123456
        # 7. Phụ huynh Tuấn: phuhuynh.tuan@beeacademy.edu.vn / Bee@123456
        users_sql = """
        INSERT INTO profiles (email, password_hash, full_name, phone, role, grade, status, teacher_approval_status, email_verified, bio)
        VALUES 
        ('admin@beeacademy.edu.vn', crypt('Bee@123456', gen_salt('bf', 12)), 'Quản Trị Viên BeeAcademy', '0901234567', 'admin', NULL, 'active', 'none', TRUE, 'Ban quản trị nền tảng giáo dục trực tuyến K-12 BeeAcademy.'),
        ('thayduc.toan@beeacademy.edu.vn', crypt('Bee@123456', gen_salt('bf', 12)), 'Thầy Hoàng Minh Đức', '0912345678', 'teacher', NULL, 'active', 'approved', TRUE, 'Thạc sĩ Toán học - Giáo viên Chuyên Toán với hơn 12 năm kinh nghiệm luyện thi THPT Quốc Gia.'),
        ('cophan.vatly@beeacademy.edu.vn', crypt('Bee@123456', gen_salt('bf', 12)), 'Cô Phan Thanh Thảo', '0923456789', 'teacher', NULL, 'active', 'approved', TRUE, 'Giáo viên Vật lý K-12, giải Nhất Giáo viên Dạy giỏi Cấp Thành Phố.'),
        ('thaythanh.hoa@beeacademy.edu.vn', crypt('Bee@123456', gen_salt('bf', 12)), 'Thầy Nguyễn Tiến Thành', '0934567890', 'teacher', NULL, 'active', 'approved', TRUE, 'Chuyên gia Hóa học Hữu cơ & Vô cơ trọng điểm.'),
        ('hocsinh.nam@beeacademy.edu.vn', crypt('Bee@123456', gen_salt('bf', 12)), 'Nguyễn Văn Nam', '0945678901', 'student', 12, 'active', 'none', TRUE, 'Học sinh lớp 12 - Mục tiêu đỗ Đại học Bách Khoa khối A00.'),
        ('hocsinh.mai@beeacademy.edu.vn', crypt('Bee@123456', gen_salt('bf', 12)), 'Lê Ngọc Mai', '0956789012', 'student', 9, 'active', 'none', TRUE, 'Học sinh lớp 9 - Ôn thi vào lớp 10 Chuyên Toán.'),
        ('phuhuynh.tuan@beeacademy.edu.vn', crypt('Bee@123456', gen_salt('bf', 12)), 'Nguyễn Minh Tuấn', '0967890123', 'parent', NULL, 'active', 'none', TRUE, 'Phụ huynh của em Nguyễn Văn Nam.')
        ON CONFLICT (email) DO UPDATE SET 
            password_hash = crypt('Bee@123456', gen_salt('bf', 12)),
            full_name = EXCLUDED.full_name,
            role = EXCLUDED.role,
            status = 'active',
            teacher_approval_status = EXCLUDED.teacher_approval_status,
            email_verified = TRUE;
        """
        cur.execute(users_sql)

        # Cập nhật cả tài khoản thanhdatvv05@gmail.com thành admin nếu đã đăng ký qua Google
        cur.execute("""
            UPDATE profiles 
            SET role = 'admin', email_verified = TRUE 
            WHERE email = 'thanhdatvv05@gmail.com';
        """)

        # Lấy ID các tài khoản
        cur.execute("SELECT id, email FROM profiles;")
        user_map = {row[1]: str(row[0]) for row in cur.fetchall()}
        admin_id = user_map['admin@beeacademy.edu.vn']
        thay_duc_id = user_map['thayduc.toan@beeacademy.edu.vn']
        co_phan_id = user_map['cophan.vatly@beeacademy.edu.vn']
        thay_thanh_id = user_map['thaythanh.hoa@beeacademy.edu.vn']
        hs_nam_id = user_map['hocsinh.nam@beeacademy.edu.vn']
        hs_mai_id = user_map['hocsinh.mai@beeacademy.edu.vn']
        ph_tuan_id = user_map['phuhuynh.tuan@beeacademy.edu.vn']

        print("📚 2. Khởi tạo/Cập nhật Danh mục Môn học (Categories)...")
        cur.execute("SELECT id, slug FROM categories;")
        cat_map = {row[1]: str(row[0]) for row in cur.fetchall()}
        toan_cat_id = cat_map['toan-hoc']
        khtn_cat_id = cat_map['khoa-hoc-tu-nhien']
        tienganh_cat_id = cat_map['tieng-anh']
        tinhoc_cat_id = cat_map['tin-hoc-lap-trinh']
        nguvan_cat_id = cat_map['ngu-van']

        print("📖 3. Khởi tạo Khóa học K-12 (Courses)...")
        # 6 Khóa học mẫu chuẩn
        courses_data = [
            (
                thay_duc_id, toan_cat_id,
                "Toán 12 — Chinh Phục Khảo Sát Hàm Số, Đạo Hàm & Tích Phân Ứng Dụng",
                "toan-12-khao-sat-ham-so-dao-ham-tich-phan",
                "Khóa học chuyên sâu trang bị đầy đủ tư duy giải nhanh 50 câu trắc nghiệm Toán THPT Quốc Gia từ nhận biết, thông hiểu đến vận dụng cao chuyên đề Hàm số và Tích phân.",
                "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800&auto=format&fit=crop&q=80",
                299000, 249000, [12], 'published', 2, 6, 11700, 4.95, 28
            ),
            (
                co_phan_id, khtn_cat_id,
                "Vật Lý 12 — Nắm Trọn Điểm 9+ Dao Động Cơ & Sóng Cơ Học",
                "vat-ly-12-dao-dong-co-va-song-co",
                "Hệ thống hóa toàn bộ công thức dao động điều hòa, con lắc lò xo, con lắc đơn, giao thoa sóng cơ và bài tập đồ thị thực chiến.",
                "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=800&auto=format&fit=crop&q=80",
                250000, 199000, [12], 'published', 2, 4, 7200, 4.88, 19
            ),
            (
                thay_thanh_id, khtn_cat_id,
                "Hóa Học 12 — Chuyên Đề Este, Lipit & Hợp Chất Hữu Cơ Chứa Nitơ",
                "hoa-hoc-12-chuyen-de-este-lipit-hop-chat-chua-nito",
                "Kỹ thuật giải bài tập thủy phân este đa chức, chất béo và biện luận chuỗi phản ứng Amin, Amino Axit, Peptit trọng tâm kỳ thi tốt nghiệp.",
                "https://images.unsplash.com/photo-1603126857599-f6e157fa2fe6?w=800&auto=format&fit=crop&q=80",
                199000, 169000, [12], 'published', 2, 4, 6800, 4.92, 14
            ),
            (
                thay_duc_id, toan_cat_id,
                "Toán 9 — Chiến Thuật Luyện Thi Vào Lớp 10 Chuyên & Công Lập",
                "toan-9-luyen-thi-vao-lop-10-chuyen-va-cong-lap",
                "Rèn luyện kỹ năng giải phương trình, hệ phương trình, bất đẳng thức Cô-si và bài toán hình học quỹ tích phân loại thí sinh.",
                "https://images.unsplash.com/photo-1596495578065-6e0763fa1178?w=800&auto=format&fit=crop&q=80",
                220000, 180000, [9], 'published', 2, 4, 6400, 4.90, 22
            ),
            (
                thay_duc_id, toan_cat_id,
                "Toán 6 — Khởi Đầu Vững Chắc: Số Học & Hình Học Trực Quan",
                "toan-6-khoi-dau-vung-chac-so-hoc-hinh-hoc",
                "Khóa học nền tảng miễn phí dành cho học sinh lớp 6 làm quen với tập hợp, số nguyên, phân số và các hình học phẳng trực quan.",
                "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=800&auto=format&fit=crop&q=80",
                0, 0, [6], 'published', 1, 2, 3600, 5.00, 35
            ),
            (
                co_phan_id, tienganh_cat_id,
                "Tiếng Anh 10 — Làm Chủ Ngữ Pháp B1–B2 & Từ Vựng Chuyên Đề",
                "tieng-anh-10-lam-chu-ngu-phap-b1-b2",
                "Hệ thống các thì trong tiếng Anh, mệnh đề quan hệ, câu điều kiện và mở rộng vốn từ vựng theo chủ điểm sách giáo khoa mới Global Success.",
                "https://images.unsplash.com/photo-1546410531-bb4caa6b424d?w=800&auto=format&fit=crop&q=80",
                180000, 150000, [10], 'published', 2, 4, 6000, 4.85, 12
            )
        ]

        course_map = {}
        for c in courses_data:
            cur.execute("""
                INSERT INTO courses (
                    teacher_id, category_id, title, slug, description, thumbnail_url,
                    price_vnd, sale_price_vnd, grades, status, total_chapters, total_lessons,
                    total_duration_sec, avg_rating, review_count, published_at
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())
                ON CONFLICT (slug) DO UPDATE SET
                    title = EXCLUDED.title,
                    description = EXCLUDED.description,
                    thumbnail_url = EXCLUDED.thumbnail_url,
                    price_vnd = EXCLUDED.price_vnd,
                    sale_price_vnd = EXCLUDED.sale_price_vnd,
                    grades = EXCLUDED.grades,
                    status = EXCLUDED.status,
                    total_chapters = EXCLUDED.total_chapters,
                    total_lessons = EXCLUDED.total_lessons,
                    total_duration_sec = EXCLUDED.total_duration_sec,
                    avg_rating = EXCLUDED.avg_rating,
                    review_count = EXCLUDED.review_count,
                    published_at = NOW()
                RETURNING id, slug;
            """, c)
            res = cur.fetchone()
            course_map[res[1]] = str(res[0])

        toan12_course_id = course_map["toan-12-khao-sat-ham-so-dao-ham-tich-phan"]
        vatly12_course_id = course_map["vat-ly-12-dao-dong-co-va-song-co"]
        toan9_course_id = course_map["toan-9-luyen-thi-vao-lop-10-chuyen-va-cong-lap"]
        toan6_course_id = course_map["toan-6-khoi-dau-vung-chac-so-hoc-hinh-hoc"]

        print("📑 4. Khởi tạo Chương & Bài giảng & Tài liệu thật (Chapters, Lessons & Documents)...")
        from seed_curriculum_and_documents import COURSES_DATA, PDF_URL_1, PDF_URL_2, PDF_URL_3

        for c in COURSES_DATA:
            c_id = c["id"]
            cur.execute("DELETE FROM chapters WHERE course_id = %s;", (c_id,))
            c_chapters_count = len(c["chapters"])
            c_lessons_count = 0
            c_duration = 0

            for ch in c["chapters"]:
                cur.execute("""
                    INSERT INTO chapters (course_id, title, description, position)
                    VALUES (%s, %s, %s, %s)
                    RETURNING id;
                """, (c_id, ch["title"], ch["description"], ch["position"]))
                ch_id = str(cur.fetchone()[0])

                if c_id == toan12_course_id and ch["position"] == 1:
                    ch1_id = ch_id
                elif c_id == toan12_course_id and ch["position"] == 2:
                    ch2_id = ch_id

                for l in ch["lessons"]:
                    c_lessons_count += 1
                    c_duration += l["duration_sec"]
                    cur.execute("""
                        INSERT INTO lessons (chapter_id, title, description, position, is_free_preview, video_storage_path, duration_sec, documents)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s);
                    """, (
                        ch_id, l["title"], l["description"], l["position"],
                        l["is_free_preview"], l["video_url"], l["duration_sec"],
                        json.dumps(l["documents"])
                    ))

            cur.execute("""
                UPDATE courses SET
                    total_chapters = %s, total_lessons = %s, total_duration_sec = %s
                WHERE id = %s;
            """, (c_chapters_count, c_lessons_count, c_duration, c_id))

        print("  ✓ Đã nạp thành công toàn bộ Chương, Bài học & Tài liệu thật cho 6 khóa học!")

        print("❓ 5. Khởi tạo Ngân hàng Câu hỏi trắc nghiệm LaTeX (Questions & Choices)...")
        # 5 Câu hỏi trắc nghiệm Toán 12 có công thức LaTeX
        questions_data = [
            (
                thay_duc_id, toan_cat_id, ch1_id,
                "Cho hàm số $y = f(x)$ có bảng biến thiên với $f'(x) = 0$ tại $x = -1$ và $x = 2$. Biết $f'(x) > 0$ trên $(-1; 2)$ và $f'(x) < 0$ trên $(-\\infty; -1) \\cup (2; +\\infty)$. Mệnh đề nào sau đây đúng?",
                "Hàm số đồng biến trên khoảng $(-1; 2)$ vì $f'(x) > 0$ với mọi $x \\in (-1; 2)$.",
                "easy",
                Json([
                    {"id": "A", "content": "Hàm số đồng biến trên khoảng $(-1; 2)$", "isCorrect": True, "position": 1},
                    {"id": "B", "content": "Hàm số nghịch biến trên khoảng $(-1; 2)$", "isCorrect": False, "position": 2},
                    {"id": "C", "content": "Hàm số đạt cực đại tại $x = -1$", "isCorrect": False, "position": 3},
                    {"id": "D", "content": "Hàm số đạt cực tiểu tại $x = 2$", "isCorrect": False, "position": 4}
                ]),
                ['toan12', 'hamso', 'don-dieu']
            ),
            (
                thay_duc_id, toan_cat_id, ch1_id,
                "Giá trị nhỏ nhất của hàm số $y = x^3 - 3x + 1$ trên đoạn $[0; 2]$ bằng bao nhiêu?",
                "Ta có $y' = 3x^2 - 3 = 0 \\iff x = 1$ (nhận) hoặc $x = -1$ (loại). Tính giá trị: $y(0) = 1$, $y(1) = -1$, $y(2) = 3$. Vậy $\\min_{[0;2]} y = -1$ tại $x = 1$.",
                "medium",
                Json([
                    {"id": "A", "content": "$\\min_{[0;2]} y = -1$", "isCorrect": True, "position": 1},
                    {"id": "B", "content": "$\\min_{[0;2]} y = 1$", "isCorrect": False, "position": 2},
                    {"id": "C", "content": "$\\min_{[0;2]} y = 3$", "isCorrect": False, "position": 3},
                    {"id": "D", "content": "$\\min_{[0;2]} y = 0$", "isCorrect": False, "position": 4}
                ]),
                ['toan12', 'gtln-gtnn']
            ),
            (
                thay_duc_id, toan_cat_id, ch1_id,
                "Đồ thị hàm số $y = \\frac{2x - 3}{x + 1}$ có đường tiệm cận đứng và tiệm cận ngang lần lượt là:",
                "Tiệm cận đứng là nghiệm của mẫu: $x + 1 = 0 \\iff x = -1$. Tiệm cận ngang là $\\lim_{x \\to \\pm\\infty} y = \\frac{2}{1} = 2$.",
                "easy",
                Json([
                    {"id": "A", "content": "$x = -1$ và $y = 2$", "isCorrect": True, "position": 1},
                    {"id": "B", "content": "$x = 1$ và $y = 2$", "isCorrect": False, "position": 2},
                    {"id": "C", "content": "$x = -1$ và $y = -3$", "isCorrect": False, "position": 3},
                    {"id": "D", "content": "$x = 2$ và $y = -1$", "isCorrect": False, "position": 4}
                ]),
                ['toan12', 'tiem-can']
            ),
            (
                thay_duc_id, toan_cat_id, ch2_id,
                "Tính tích phân $I = \\int_{0}^{1} (2x + e^x) \\, dx$. Kết quả đúng là:",
                "Ta có nguyên hàm: $\\int (2x + e^x) dx = x^2 + e^x$. Do đó: $I = [x^2 + e^x]_0^1 = (1 + e) - (0 + 1) = e$.",
                "medium",
                Json([
                    {"id": "A", "content": "$I = e$", "isCorrect": True, "position": 1},
                    {"id": "B", "content": "$I = e + 1$", "isCorrect": False, "position": 2},
                    {"id": "C", "content": "$I = e - 1$", "isCorrect": False, "position": 3},
                    {"id": "D", "content": "$I = 2e$", "isCorrect": False, "position": 4}
                ]),
                ['toan12', 'tich-phan']
            ),
            (
                thay_duc_id, toan_cat_id, ch2_id,
                "Tìm họ tất cả các nguyên hàm của hàm số $f(x) = \\sin 2x$ trên $\\mathbb{R}$:",
                "Ta có $\\int \\sin 2x \\, dx = -\\frac{1}{2} \\cos 2x + C$.",
                "easy",
                Json([
                    {"id": "A", "content": "$F(x) = -\\frac{1}{2} \\cos 2x + C$", "isCorrect": True, "position": 1},
                    {"id": "B", "content": "$F(x) = \\frac{1}{2} \\cos 2x + C$", "isCorrect": False, "position": 2},
                    {"id": "C", "content": "$F(x) = -2 \\cos 2x + C$", "isCorrect": False, "position": 3},
                    {"id": "D", "content": "$F(x) = 2 \\cos 2x + C$", "isCorrect": False, "position": 4}
                ]),
                ['toan12', 'nguyen-ham']
            )
        ]

        for q in questions_data:
            cur.execute("""
                INSERT INTO questions (
                    teacher_id, category_id, chapter_id, content, explanation, difficulty, type, choices, tags, status
                ) VALUES (%s, %s, %s, %s, %s, %s, 'multiple_choice', %s, %s, 'active');
            """, q)

        print("📝 6. Cấu hình Ma trận Đề kiểm tra Chương (Assessments - Quiz)...")
        cur.execute("""
            INSERT INTO assessments (
                course_id, chapter_id, teacher_id, type, title, description,
                time_limit_minutes, passing_score, max_attempts, config, active
            ) VALUES (
                %s, %s, %s, 'QUIZ',
                'Kiểm tra Đánh giá Chương 1: Ứng dụng đạo hàm khảo sát hàm số',
                'Đề kiểm tra ngẫu nhiên 3 câu hỏi trắc nghiệm kiểm tra độ hiểu bài sau khi xem xong video bài giảng.',
                15, 70.00, 5,
                '{"easyCount": 2, "mediumCount": 1, "hardCount": 0, "shuffleChoices": true}'::jsonb,
                TRUE
            )
            ON CONFLICT DO NOTHING;
        """, (toan12_course_id, ch1_id, thay_duc_id))

        print("🎓 7. Khởi tạo Tiến độ Học tập & Chứng chỉ số (Enrollments & Certificates)...")
        # Học sinh Nam đã hoàn thành Khóa Toán 6 (100%)
        cur.execute("""
            INSERT INTO enrollments (
                student_id, course_id, completed_lessons, total_lessons, progress_percent,
                final_exam_passed, completed_at, enrolled_at
            ) VALUES (
                %s, %s, 2, 2, 100.00, TRUE, NOW() - INTERVAL '2 days', NOW() - INTERVAL '7 days'
            )
            ON CONFLICT (student_id, course_id) DO UPDATE SET
                completed_lessons = 2, total_lessons = 2, progress_percent = 100.00,
                final_exam_passed = TRUE, completed_at = NOW() - INTERVAL '2 days'
            RETURNING id;
        """, (hs_nam_id, toan6_course_id))
        enr_toan6_id = str(cur.fetchone()[0])

        # Cấp Chứng chỉ số cho học sinh Nam
        cur.execute("""
            INSERT INTO certificates (
                enrollment_id, student_id, course_id, serial_number, student_name_snapshot,
                course_title_snapshot, final_exam_score, pdf_url, issued_at
            ) VALUES (
                %s, %s, %s, 'BEE-2026-MATH06-9988', 'Nguyễn Văn Nam',
                'Toán 6 — Khởi Đầu Vững Chắc: Số Học & Hình Học Trực Quan',
                95.00, 'https://res.cloudinary.com/hohjixsz/image/upload/v1/certificates/sample.pdf',
                NOW() - INTERVAL '2 days'
            )
            ON CONFLICT (enrollment_id) DO UPDATE SET
                serial_number = 'BEE-2026-MATH06-9988',
                final_exam_score = 95.00;
        """, (enr_toan6_id, hs_nam_id, toan6_course_id))

        # Học sinh Nam đang học Khóa Toán 12 (66% - hoàn thành 2/3 bài chương 1)
        cur.execute("""
            INSERT INTO enrollments (
                student_id, course_id, completed_lessons, total_lessons, progress_percent,
                final_exam_passed, enrolled_at
            ) VALUES (
                %s, %s, 2, 6, 33.33, FALSE, NOW() - INTERVAL '3 days'
            )
            ON CONFLICT (student_id, course_id) DO UPDATE SET
                completed_lessons = 2, progress_percent = 33.33;
        """, (hs_nam_id, toan12_course_id))

        # Ghi nhận tiến độ chi tiết bài 1 (Anti-Skip đạt 100%)
        if lesson_ids:
            cur.execute("""
                INSERT INTO lesson_progress (
                    student_id, lesson_id, course_id, watched_seconds, last_position_sec,
                    watch_segments, is_completed, completed_at
                ) VALUES (
                    %s, %s, %s, 1800, 1800,
                    '[{"start": 0, "end": 1800}]'::jsonb,
                    TRUE, NOW() - INTERVAL '1 day'
                )
                ON CONFLICT (student_id, lesson_id) DO UPDATE SET
                    is_completed = TRUE, watched_seconds = 1800;
            """, (hs_nam_id, lesson_ids[0], toan12_course_id))

        print("💳 8. Khởi tạo Đơn hàng PayOS VietQR & Phân chia Doanh thu (Orders & Revenue Splits)...")
        # Đơn 1: Đã thanh toán (PAID) khóa Toán 12 cho Nam
        order_code_1 = 100001
        pay_ref_1 = f"PAY-{order_code_1}"
        cur.execute("""
            INSERT INTO orders (
                student_id, payment_ref, order_code, payment_method, status,
                total_amount_vnd, discount_amount_vnd, expires_at, paid_at, created_at
            ) VALUES (
                %s, %s, %s, 'PAYOS', 'paid',
                249000, 50000, NOW() + INTERVAL '1 day', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'
            )
            ON CONFLICT (payment_ref) DO UPDATE SET
                status = 'paid', paid_at = NOW() - INTERVAL '3 days'
            RETURNING id;
        """, (hs_nam_id, pay_ref_1, order_code_1))
        order_1_id = str(cur.fetchone()[0])

        # Chi tiết đơn 1 (order_items)
        cur.execute("""
            INSERT INTO order_items (
                order_id, course_id, teacher_id, course_title_snapshot, price_at_purchase,
                platform_fee_rate, platform_fee_vnd, teacher_amount_vnd, item_status
            ) VALUES (
                %s, %s, %s, 'Toán 12 — Chinh Phục Khảo Sát Hàm Số, Đạo Hàm & Tích Phân Ứng Dụng',
                249000, 20.00, 49800, 199200, 'paid'
            )
            ON CONFLICT DO NOTHING;
        """, (order_1_id, toan12_course_id, thay_duc_id))

        # Đơn 2: Đang chờ thanh toán (PENDING) để người dùng test quét mã VietQR và kiểm tra bộ đếm ngược
        order_code_2 = 100002
        pay_ref_2 = f"PAY-{order_code_2}"
        cur.execute("""
            INSERT INTO orders (
                student_id, payment_ref, order_code, payment_method, status,
                total_amount_vnd, discount_amount_vnd, expires_at, created_at
            ) VALUES (
                %s, %s, %s, 'PAYOS', 'pending',
                199000, 51000, NOW() + INTERVAL '15 minutes', NOW()
            )
            ON CONFLICT (payment_ref) DO UPDATE SET
                status = 'pending', expires_at = NOW() + INTERVAL '15 minutes'
            RETURNING id;
        """, (hs_nam_id, pay_ref_2, order_code_2))
        order_2_id = str(cur.fetchone()[0])

        cur.execute("""
            INSERT INTO order_items (
                order_id, course_id, teacher_id, course_title_snapshot, price_at_purchase,
                platform_fee_rate, platform_fee_vnd, teacher_amount_vnd, item_status
            ) VALUES (
                %s, %s, %s, 'Vật Lý 12 — Nắm Trọn Điểm 9+ Dao Động Cơ & Sóng Cơ Học',
                199000, 20.00, 39800, 159200, 'pending'
            )
            ON CONFLICT DO NOTHING;
        """, (order_2_id, vatly12_course_id, co_phan_id))

        print("👨‍👩‍👧 9. Khởi tạo Liên kết Phụ huynh - Học sinh (Parent Student Link)...")
        # Bác Tuấn liên kết ACTIVE với Nam (xác nhận consent)
        cur.execute("""
            INSERT INTO parent_student_links (
                parent_id, student_id, student_email, invite_token, status,
                consent_granted, invite_expires_at, linked_at
            ) VALUES (
                %s, %s, 'hocsinh.nam@beeacademy.edu.vn', 'BEE999', 'active',
                TRUE, NOW() + INTERVAL '30 days', NOW() - INTERVAL '5 days'
            )
            ON CONFLICT (invite_token) DO UPDATE SET
                status = 'active', consent_granted = TRUE;
        """, (ph_tuan_id, hs_nam_id))

        # Bác Tuấn có 1 lời mời PENDING cho em Mai với mã mời BEE123 để học sinh test nhập mã
        cur.execute("""
            INSERT INTO parent_student_links (
                parent_id, student_id, student_email, invite_token, status,
                consent_granted, invite_expires_at
            ) VALUES (
                %s, %s, 'hocsinh.mai@beeacademy.edu.vn', 'BEE123', 'pending',
                FALSE, NOW() + INTERVAL '7 days'
            )
            ON CONFLICT (invite_token) DO UPDATE SET
                status = 'pending', consent_granted = FALSE;
        """, (ph_tuan_id, hs_mai_id))

        print("🏦 10. Khởi tạo Tài khoản Ngân hàng Giáo viên & Quyết toán (Teacher Bank & Payouts)...")
        # Thầy Đức: MBBank đã thẩm định (VERIFIED)
        cur.execute("""
            INSERT INTO teacher_bank_accounts (
                teacher_id, bank_name, account_number_encrypted, account_number_masked,
                account_holder, branch, verify_status, otp_verified, verified_at
            ) VALUES (
                %s, 'Ngân hàng Quân Đội (MBBank)',
                'ENC_GCM_0348756972_SECURE', '***56972',
                'HOANG MINH DUC', 'Chi nhánh Hà Nội',
                'verified', TRUE, NOW() - INTERVAL '10 days'
            )
            ON CONFLICT (teacher_id) DO UPDATE SET
                verify_status = 'verified', otp_verified = TRUE;
        """, (thay_duc_id,))

        # Kỳ quyết toán tháng 10/2026: 1.200.000đ (trạng thái pending chờ Admin xác nhận UNC)
        cur.execute("""
            INSERT INTO payout_periods (
                teacher_id, month_year, status, total_gross_vnd, total_platform_fee_vnd,
                total_teacher_vnd, transaction_count, created_at
            ) VALUES (
                %s, '2026-10', 'pending', 1500000, 300000, 1200000, 6, NOW() - INTERVAL '2 days'
            )
            ON CONFLICT (teacher_id, month_year) DO UPDATE SET
                status = 'pending', total_teacher_vnd = 1200000;
        """, (thay_duc_id,))

        print("⭐ 11. Khởi tạo Đánh giá & Nhận xét Khóa học (Course Reviews)...")
        cur.execute("""
            INSERT INTO course_reviews (course_id, student_id, rating, comment, created_at)
            VALUES 
            (%s, %s, 5, 'Khóa học của Thầy Đức thực sự tuyệt vời! Bài giảng rất trực quan, công thức Toán LaTeX hiển thị rõ nét và bài tập bám sát đề thi THPT Quốc Gia.', NOW() - INTERVAL '2 days'),
            (%s, %s, 5, 'Em đã xem hết bài học thử và đăng ký ngay. Thầy giải bài tập cực kỳ có tâm!', NOW() - INTERVAL '1 day')
            ON CONFLICT (course_id, student_id) DO NOTHING;
        """, (toan12_course_id, hs_nam_id, toan12_course_id, hs_mai_id))

        print("💬 12. Khởi tạo Khiếu nại Hỗ trợ & Thông báo Hệ thống (Support & Notifications)...")
        # Khiếu nại mẫu
        cur.execute("""
            INSERT INTO complaints (
                sender_id, sender_role, title, category, priority, status, last_activity_at
            ) VALUES (
                %s, 'student', 'Cần hỗ trợ về tài liệu bài giảng chuyên đề Tích phân',
                'course_content', 'medium', 'open', NOW()
            )
            RETURNING id;
        """, (hs_nam_id,))
        complaint_id = str(cur.fetchone()[0])

        cur.execute("""
            INSERT INTO complaint_messages (
                complaint_id, author_id, author_role, content
            ) VALUES (
                %s, %s, 'student', 'Em chào thầy cô, cho em hỏi tài liệu PDF bài giảng số 2 có kèm thêm phần bài tập rèn luyện về nhà không ạ?'
            );
        """, (complaint_id, hs_nam_id))

        # Thông báo mẫu cho học sinh Nam
        cur.execute("""
            INSERT INTO notifications (recipient_id, title, message, type, action_url)
            VALUES 
            (%s, 'Chào mừng đến với BeeAcademy!', 'Tài khoản của bạn đã được kích hoạt thành công. Bắt đầu khám phá kho bài giảng ngay hôm nay.', 'general', '/courses'),
            (%s, 'Thanh toán thành công đơn hàng #100001', 'Bạn đã thanh toán thành công khóa học Toán 12 qua VietQR. Chúc bạn học tập tốt!', 'payment_success', '/learning/courses/' || %s),
            (%s, 'Cấp chứng chỉ hoàn thành khóa học', 'Chúc mừng bạn đã hoàn thành khóa học Toán 6 và được cấp Chứng chỉ số BEE-2026-MATH06-9988!', 'course_update', '/learning/certificates');
        """, (hs_nam_id, hs_nam_id, toan12_course_id, hs_nam_id))

        conn.commit()
        print("\n🎉 HOÀN THÀNH NHẬP DỮ LIỆU THỬ NGHIỆM THÀNH CÔNG 100%!")

    except Exception as e:
        conn.rollback()
        print(f"\n❌ LỖI TRONG QUÁ TRÌNH NHẬP DỮ LIỆU: {e}")
        raise e
    finally:
        cur.close()
        conn.close()

if __name__ == '__main__':
    run_seed()
