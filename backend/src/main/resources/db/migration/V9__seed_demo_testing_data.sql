-- ============================================================================
-- V9: Dữ liệu Khởi tạo Thử nghiệm Hệ thống (Seed Demo & Testing Data)
-- Phủ đầy đủ 8 Phân hệ (Module) & 4 Vai trò người dùng (Admin, Teacher, Student, Parent)
-- Mật khẩu mặc định cho toàn bộ tài khoản thử nghiệm: Bee@123456
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Tài khoản Người dùng Mẫu (Profiles)
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

-- 2. Khóa học Mẫu K-12 (Courses)
DO $$
DECLARE
    v_thay_duc UUID;
    v_co_phan UUID;
    v_thay_thanh UUID;
    v_toan_cat UUID;
    v_khtn_cat UUID;
    v_tienganh_cat UUID;
    v_toan12_id UUID;
    v_vatly12_id UUID;
    v_toan6_id UUID;
    v_toan9_id UUID;
    v_ch1_id UUID;
    v_ch2_id UUID;
    v_toan6_ch1 UUID;
    v_toan6_ch2 UUID;
    v_toan6_ch3 UUID;
    v_l1_id UUID;
    v_nam_id UUID;
    v_mai_id UUID;
    v_tuan_id UUID;
    v_enr_toan6 UUID;
    v_ord1_id UUID;
    v_ord2_id UUID;
    v_comp_id UUID;
BEGIN
    SELECT id INTO v_thay_duc FROM profiles WHERE email = 'thayduc.toan@beeacademy.edu.vn';
    SELECT id INTO v_co_phan FROM profiles WHERE email = 'cophan.vatly@beeacademy.edu.vn';
    SELECT id INTO v_thay_thanh FROM profiles WHERE email = 'thaythanh.hoa@beeacademy.edu.vn';
    SELECT id INTO v_nam_id FROM profiles WHERE email = 'hocsinh.nam@beeacademy.edu.vn';
    SELECT id INTO v_mai_id FROM profiles WHERE email = 'hocsinh.mai@beeacademy.edu.vn';
    SELECT id INTO v_tuan_id FROM profiles WHERE email = 'phuhuynh.tuan@beeacademy.edu.vn';

    SELECT id INTO v_toan_cat FROM categories WHERE slug = 'toan-hoc';
    SELECT id INTO v_khtn_cat FROM categories WHERE slug = 'khoa-hoc-tu-nhien';
    SELECT id INTO v_tienganh_cat FROM categories WHERE slug = 'tieng-anh';

    -- Khóa 1: Toán 12
    INSERT INTO courses (
        teacher_id, category_id, title, slug, description, thumbnail_url,
        price_vnd, sale_price_vnd, grades, status, total_chapters, total_lessons,
        total_duration_sec, avg_rating, review_count, published_at
    ) VALUES (
        v_thay_duc, v_toan_cat,
        'Toán 12 — Chinh Phục Khảo Sát Hàm Số, Đạo Hàm & Tích Phân Ứng Dụng',
        'toan-12-khao-sat-ham-so-dao-ham-tich-phan',
        'Khóa học chuyên sâu trang bị đầy đủ tư duy giải nhanh 50 câu trắc nghiệm Toán THPT Quốc Gia từ nhận biết, thông hiểu đến vận dụng cao chuyên đề Hàm số và Tích phân.',
        'https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800&auto=format&fit=crop&q=80',
        299000, 249000, '{12}', 'published', 2, 6, 11700, 4.95, 28, NOW()
    ) ON CONFLICT (slug) DO UPDATE SET published_at = NOW()
    RETURNING id INTO v_toan12_id;

    -- Khóa 2: Vật Lý 12
    INSERT INTO courses (
        teacher_id, category_id, title, slug, description, thumbnail_url,
        price_vnd, sale_price_vnd, grades, status, total_chapters, total_lessons,
        total_duration_sec, avg_rating, review_count, published_at
    ) VALUES (
        v_co_phan, v_khtn_cat,
        'Vật Lý 12 — Nắm Trọn Điểm 9+ Dao Động Cơ & Sóng Cơ Học',
        'vat-ly-12-dao-dong-co-va-song-co',
        'Hệ thống hóa toàn bộ công thức dao động điều hòa, con lắc lò xo, con lắc đơn, giao thoa sóng cơ và bài tập đồ thị thực chiến.',
        'https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=800&auto=format&fit=crop&q=80',
        250000, 199000, '{12}', 'published', 2, 4, 7200, 4.88, 19, NOW()
    ) ON CONFLICT (slug) DO UPDATE SET published_at = NOW()
    RETURNING id INTO v_vatly12_id;

    -- Khóa 3: Toán 6 Miễn phí
    INSERT INTO courses (
        teacher_id, category_id, title, slug, description, thumbnail_url,
        price_vnd, sale_price_vnd, grades, status, total_chapters, total_lessons,
        total_duration_sec, avg_rating, review_count, published_at
    ) VALUES (
        v_thay_duc, v_toan_cat,
        'Toán 6 — Khởi Đầu Vững Chắc: Số Học & Hình Học Trực Quan',
        'toan-6-khoi-dau-vung-chac-so-hoc-hinh-hoc',
        'Khóa học nền tảng miễn phí dành cho học sinh lớp 6 làm quen với tập hợp, số nguyên, phân số và các hình học phẳng trực quan.',
        'https://images.unsplash.com/photo-1509228468518-180dd4864904?w=800&auto=format&fit=crop&q=80',
        0, 0, '{6}', 'published', 3, 6, 10100, 5.00, 35, NOW()
    ) ON CONFLICT (slug) DO UPDATE SET total_chapters = 3, total_lessons = 6, total_duration_sec = 10100, published_at = NOW()
    RETURNING id INTO v_toan6_id;

    -- Khóa 4: Toán 9
    INSERT INTO courses (
        teacher_id, category_id, title, slug, description, thumbnail_url,
        price_vnd, sale_price_vnd, grades, status, total_chapters, total_lessons,
        total_duration_sec, avg_rating, review_count, published_at
    ) VALUES (
        v_thay_duc, v_toan_cat,
        'Toán 9 — Chiến Thuật Luyện Thi Vào Lớp 10 Chuyên & Công Lập',
        'toan-9-luyen-thi-vao-lop-10-chuyen-va-cong-lap',
        'Rèn luyện kỹ năng giải phương trình, hệ phương trình, bất đẳng thức Cô-si và bài toán hình học quỹ tích phân loại thí sinh.',
        'https://images.unsplash.com/photo-1596495578065-6e0763fa1178?w=800&auto=format&fit=crop&q=80',
        220000, 180000, '{9}', 'published', 2, 4, 6400, 4.90, 22, NOW()
    ) ON CONFLICT (slug) DO UPDATE SET published_at = NOW()
    RETURNING id INTO v_toan9_id;

    -- 3. Chương & Bài giảng Khóa Toán 12
    DELETE FROM chapters WHERE course_id = v_toan12_id;

    INSERT INTO chapters (course_id, title, description, position)
    VALUES (v_toan12_id, 'Chương 1: Khảo sát hàm số và ứng dụng đạo hàm', 'Ứng dụng đạo hàm khảo sát tính đơn điệu, cực trị, tiệm cận và đồ thị hàm số', 1)
    RETURNING id INTO v_ch1_id;

    INSERT INTO chapters (course_id, title, description, position)
    VALUES (v_toan12_id, 'Chương 2: Nguyên hàm, Tích phân và Ứng dụng hình học', 'Phương pháp tính nguyên hàm, tích phân và ứng dụng tính diện tích, thể tích', 2)
    RETURNING id INTO v_ch2_id;

    -- Bài giảng Chương 1
    INSERT INTO lessons (chapter_id, title, description, position, is_free_preview, video_storage_path, duration_sec, documents)
    VALUES 
        (v_ch1_id, 'Bài 1: Tính đơn điệu và các điểm cực trị của hàm số', 'Lý thuyết đạo hàm cấp 1, dấu của đạo hàm và quy tắc xác định cực đại, cực tiểu.', 1, TRUE, 'https://www.youtube.com/watch?v=kYv9rE2z0wM', 1800,
         '[{"id": "7a356064-a63e-4fa0-b4f6-8c4d299403a1", "title": "Sổ tay công thức Đạo hàm & Bảng biến thiên Toán 12.pdf", "name": "Sổ tay công thức Đạo hàm & Bảng biến thiên Toán 12.pdf", "fileUrl": "https://raw.githubusercontent.com/mozilla/pdf.js/master/web/compressed.tracemonkey-pldi-09.pdf", "fileType": "pdf", "fileSizeBytes": 2540000}, {"id": "8b467175-b74f-5ab1-c5f7-9d5e300514b2", "title": "Tuyển tập 100 câu trắc nghiệm Cực trị hàm số có đáp án.pdf", "name": "Tuyển tập 100 câu trắc nghiệm Cực trị hàm số có đáp án.pdf", "fileUrl": "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", "fileType": "pdf", "fileSizeBytes": 3120000}]'::jsonb)
    RETURNING id INTO v_l1_id;

    INSERT INTO lessons (chapter_id, title, description, position, is_free_preview, video_storage_path, duration_sec, documents)
    VALUES 
        (v_ch1_id, 'Bài 2: Giá trị lớn nhất và nhỏ nhất của hàm số trên đoạn', 'Quy tắc tìm GTLN, GTNN trên đoạn [a; b], đạo hàm triệt tiêu và bài toán thực tế tối ưu hóa.', 2, FALSE, 'https://www.youtube.com/watch?v=5V_2Z1D2xvg', 2100,
         '[{"id": "9c578286-c850-6bc2-d608-ae6f411625c3", "title": "Chuyên đề Min Max hàm hợp & Kỹ thuật Casio 880BTG.pdf", "name": "Chuyên đề Min Max hàm hợp & Kỹ thuật Casio 880BTG.pdf", "fileUrl": "https://raw.githubusercontent.com/foliojs/pdfkit/master/docs/guide.pdf", "fileType": "pdf", "fileSizeBytes": 1980000}]'::jsonb),
        (v_ch1_id, 'Bài 3: Đường tiệm cận đứng, tiệm cận ngang và tiệm cận xiên', 'Kỹ thuật bấm máy Casio tìm giới hạn và nhận dạng nhanh tiệm cận từ bảng biến thiên.', 3, FALSE, 'https://www.youtube.com/watch?v=kYv9rE2z0wM', 1500,
         '[{"id": "ad689397-d961-7cd3-e719-bf70522736d4", "title": "Phương pháp nhận dạng tiệm cận và đồ thị hàm phân thức.pdf", "name": "Phương pháp nhận dạng tiệm cận và đồ thị hàm phân thức.pdf", "fileUrl": "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", "fileType": "pdf", "fileSizeBytes": 1450000}]'::jsonb),
        (v_ch2_id, 'Bài 4: Bảng nguyên hàm cơ bản và phương pháp đổi biến số', 'Nắm vững các công thức nguyên hàm hàm số lũy thừa, hàm lượng giác và hàm mũ.', 1, TRUE, 'https://www.youtube.com/watch?v=5V_2Z1D2xvg', 2400,
         '[{"id": "be790408-ea72-8de4-f820-c081633847e5", "title": "Bảng 60 công thức Nguyên hàm giải nhanh đại học.pdf", "name": "Bảng 60 công thức Nguyên hàm giải nhanh đại học.pdf", "fileUrl": "https://raw.githubusercontent.com/mozilla/pdf.js/master/web/compressed.tracemonkey-pldi-09.pdf", "fileType": "pdf", "fileSizeBytes": 2780000}]'::jsonb),
        (v_ch2_id, 'Bài 5: Phương pháp tích phân từng phần (Nhất lô, nhì đa, tam lượng, tứ mũ)', 'Quy tắc đặt u và dv, ứng dụng sơ đồ đường chéo tính nhanh tích phân hàm hỗn hợp.', 2, FALSE, 'https://www.youtube.com/watch?v=kYv9rE2z0wM', 1800,
         '[{"id": "cf801519-fb83-9ef5-0931-d192744958f6", "title": "Kỹ thuật sơ đồ đường chéo tính nhanh tích phân từng phần.pdf", "name": "Kỹ thuật sơ đồ đường chéo tính nhanh tích phân từng phần.pdf", "fileUrl": "https://raw.githubusercontent.com/foliojs/pdfkit/master/docs/guide.pdf", "fileType": "pdf", "fileSizeBytes": 1890000}]'::jsonb),
        (v_ch2_id, 'Bài 6: Ứng dụng tích phân tính diện tích hình phẳng & thể tích vật thể tròn xoay', 'Công thức diện tích giới hạn bởi 2 đường cong và thể tích khối tròn xoay quanh trục Ox.', 3, FALSE, 'https://www.youtube.com/watch?v=5V_2Z1D2xvg', 2100,
         '[{"id": "d0912620-0c94-0f06-1042-e2a3855a6907", "title": "Chuyên đề bài toán thực tế diện tích & thể tích tích phân.pdf", "name": "Chuyên đề bài toán thực tế diện tích & thể tích tích phân.pdf", "fileUrl": "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", "fileType": "pdf", "fileSizeBytes": 3450000}]'::jsonb);

    -- Chương & Bài giảng Khóa Toán 6
    DELETE FROM chapters WHERE course_id = v_toan6_id;

    INSERT INTO chapters (course_id, title, description, position)
    VALUES (v_toan6_id, 'Chương 1: Tập Hợp & Các Phép Tính Số Tự Nhiên', 'Nắm vững định nghĩa tập hợp, phần tử, lũy thừa và các phép tính cơ bản.', 1)
    RETURNING id INTO v_toan6_ch1;

    INSERT INTO chapters (course_id, title, description, position)
    VALUES (v_toan6_id, 'Chương 2: Tính Chia Hết & Số Nguyên Tố', 'Dấu hiệu chia hết cho 2, 3, 5, 9, sàng Eratosthenes và phân tích thừa số nguyên tố.', 2)
    RETURNING id INTO v_toan6_ch2;

    INSERT INTO chapters (course_id, title, description, position)
    VALUES (v_toan6_id, 'Chương 3: Hình Học Trực Quan & Đo Lường Thực Tế', 'Tam giác đều, hình vuông, lục giác đều, hình chữ nhật và hình thoi.', 3)
    RETURNING id INTO v_toan6_ch3;

    INSERT INTO lessons (chapter_id, title, description, position, is_free_preview, video_storage_path, duration_sec, documents)
    VALUES 
        (v_toan6_ch1, 'Bài 1: Tập hợp & Phần tử của tập hợp', 'Các cách cho một tập hợp, kí hiệu thuộc và không thuộc, minh họa bằng biểu đồ Ven.', 1, TRUE, 'https://www.youtube.com/watch?v=0hWq5qYJb6g', 1200,
         '[{"id": "e1a23731-1da5-1017-2153-f3b4966b7a18", "title": "Tóm tắt lý thuyết & Sơ đồ tư duy Tập hợp Toán 6.pdf", "name": "Tóm tắt lý thuyết & Sơ đồ tư duy Tập hợp Toán 6.pdf", "fileUrl": "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", "fileType": "pdf", "fileSizeBytes": 1450000}, {"id": "f2b34842-2eb6-2128-3264-04c5a77c8b29", "title": "Phiếu bài tập rèn luyện Tập hợp có đáp án chi tiết.pdf", "name": "Phiếu bài tập rèn luyện Tập hợp có đáp án chi tiết.pdf", "fileUrl": "https://raw.githubusercontent.com/mozilla/pdf.js/master/web/compressed.tracemonkey-pldi-09.pdf", "fileType": "pdf", "fileSizeBytes": 2180000}]'::jsonb),
        (v_toan6_ch1, 'Bài 2: Các phép tính cộng, trừ, nhân, chia số tự nhiên', 'Quy tắc thứ tự thực hiện phép tính, tính chất giao hoán, kết hợp và phân phối.', 2, FALSE, 'https://www.youtube.com/watch?v=5MgBikgcWnY', 1500,
         '[{"id": "03c45953-3fc7-3239-4375-15d6b88d9c3a", "title": "Chuyên đề tính nhanh & Thứ tự thực hiện phép tính.pdf", "name": "Chuyên đề tính nhanh & Thứ tự thực hiện phép tính.pdf", "fileUrl": "https://raw.githubusercontent.com/foliojs/pdfkit/master/docs/guide.pdf", "fileType": "pdf", "fileSizeBytes": 1820000}]'::jsonb),
        (v_toan6_ch1, 'Bài 3: Lũy thừa với số mũ tự nhiên', 'Định nghĩa lũy thừa bậc n, nhân và chia hai lũy thừa cùng cơ số.', 3, FALSE, 'https://www.youtube.com/watch?v=kJQP7kiw5Fk', 1800,
         '[{"id": "14d56064-40d8-434a-5486-26e7c99ea04b", "title": "Bảng quy tắc lũy thừa & Kỹ thuật tính nhẩm Toán 6.pdf", "name": "Bảng quy tắc lũy thừa & Kỹ thuật tính nhẩm Toán 6.pdf", "fileUrl": "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", "fileType": "pdf", "fileSizeBytes": 980000}]'::jsonb),
        (v_toan6_ch2, 'Bài 4: Quan hệ chia hết và dấu hiệu chia hết cho 2, 3, 5, 9', 'Tính chất chia hết của một tổng, các dấu hiệu nhận biết nhanh.', 1, FALSE, 'https://www.youtube.com/watch?v=fJ9rUzIMcZQ', 1600,
         '[{"id": "25e67175-51e9-545b-6597-37f8daafb15c", "title": "Bộ đề trắc nghiệm dấu hiệu chia hết Toán 6.pdf", "name": "Bộ đề trắc nghiệm dấu hiệu chia hết Toán 6.pdf", "fileUrl": "https://raw.githubusercontent.com/mozilla/pdf.js/master/web/compressed.tracemonkey-pldi-09.pdf", "fileType": "pdf", "fileSizeBytes": 1640000}]'::jsonb),
        (v_toan6_ch2, 'Bài 5: Số nguyên tố, hợp số & Phân tích ra thừa số nguyên tố', 'Cách tìm ước chung lớn nhất (ƯCLN) và bội chung nhỏ nhất (BCNN).', 2, FALSE, 'https://www.youtube.com/watch?v=0hWq5qYJb6g', 2000,
         '[{"id": "36f78286-62fa-656c-76a8-4809ebb0c26d", "title": "Sàng Eratosthenes & Chuyên đề bài tập số nguyên tố.pdf", "name": "Sàng Eratosthenes & Chuyên đề bài tập số nguyên tố.pdf", "fileUrl": "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", "fileType": "pdf", "fileSizeBytes": 1350000}]'::jsonb),
        (v_toan6_ch3, 'Bài 6: Nhận biết & Vẽ tam giác đều, hình vuông, lục giác đều', 'Thực hành sử dụng thước kẻ, ê-ke và compa vẽ các hình hình học trực quan.', 1, FALSE, 'https://www.youtube.com/watch?v=5MgBikgcWnY', 1500,
         '[{"id": "47089397-730b-767d-87b9-591afcc1d37e", "title": "Thực hành cắt dán và vẽ hình học trực quan Toán 6.pdf", "name": "Thực hành cắt dán và vẽ hình học trực quan Toán 6.pdf", "fileUrl": "https://raw.githubusercontent.com/foliojs/pdfkit/master/docs/guide.pdf", "fileType": "pdf", "fileSizeBytes": 3200000}]'::jsonb);

    -- 4. Ngân hàng Câu hỏi trắc nghiệm Toán 12 kèm LaTeX KaTeX
    INSERT INTO questions (teacher_id, category_id, chapter_id, content, explanation, difficulty, type, choices, tags, status)
    VALUES 
        (v_thay_duc, v_toan_cat, v_ch1_id,
        'Cho hàm số $y = f(x)$ có bảng biến thiên với $f''(x) = 0$ tại $x = -1$ và $x = 2$. Biết $f''(x) > 0$ trên $(-1; 2)$ và $f''(x) < 0$ trên $(-\\infty; -1) \\cup (2; +\\infty)$. Mệnh đề nào sau đây đúng?',
        'Hàm số đồng biến trên khoảng $(-1; 2)$ vì $f''(x) > 0$ với mọi $x \\in (-1; 2)$.',
        'easy', 'multiple_choice',
        '[{"id": "A", "content": "Hàm số đồng biến trên khoảng $(-1; 2)$", "isCorrect": true, "position": 1}, {"id": "B", "content": "Hàm số nghịch biến trên khoảng $(-1; 2)$", "isCorrect": false, "position": 2}, {"id": "C", "content": "Hàm số đạt cực đại tại $x = -1$", "isCorrect": false, "position": 3}, {"id": "D", "content": "Hàm số đạt cực tiểu tại $x = 2$", "isCorrect": false, "position": 4}]'::jsonb,
        '{"toan12", "hamso", "don-dieu"}', 'active'),
        (v_thay_duc, v_toan_cat, v_ch1_id,
        'Giá trị nhỏ nhất của hàm số $y = x^3 - 3x + 1$ trên đoạn $[0; 2]$ bằng bao nhiêu?',
        'Ta có $y'' = 3x^2 - 3 = 0 \\iff x = 1$ (nhận) hoặc $x = -1$ (loại). Tính giá trị: $y(0) = 1$, $y(1) = -1$, $y(2) = 3$. Vậy $\\min_{[0;2]} y = -1$ tại $x = 1$.',
        'medium', 'multiple_choice',
        '[{"id": "A", "content": "$\\min_{[0;2]} y = -1$", "isCorrect": true, "position": 1}, {"id": "B", "content": "$\\min_{[0;2]} y = 1$", "isCorrect": false, "position": 2}, {"id": "C", "content": "$\\min_{[0;2]} y = 3$", "isCorrect": false, "position": 3}, {"id": "D", "content": "$\\min_{[0;2]} y = 0$", "isCorrect": false, "position": 4}]'::jsonb,
        '{"toan12", "gtln-gtnn"}', 'active'),
        (v_thay_duc, v_toan_cat, v_ch1_id,
        'Đồ thị hàm số $y = \\frac{2x - 3}{x + 1}$ có đường tiệm cận đứng và tiệm cận ngang lần lượt là:',
        'Tiệm cận đứng là nghiệm của mẫu: $x + 1 = 0 \\iff x = -1$. Tiệm cận ngang là $\\lim_{x \\to \\pm\\infty} y = \\frac{2}{1} = 2$.',
        'easy', 'multiple_choice',
        '[{"id": "A", "content": "$x = -1$ và $y = 2$", "isCorrect": true, "position": 1}, {"id": "B", "content": "$x = 1$ và $y = 2$", "isCorrect": false, "position": 2}, {"id": "C", "content": "$x = -1$ và $y = -3$", "isCorrect": false, "position": 3}, {"id": "D", "content": "$x = 2$ và $y = -1$", "isCorrect": false, "position": 4}]'::jsonb,
        '{"toan12", "tiem-can"}', 'active'),
        (v_thay_duc, v_toan_cat, v_ch2_id,
        'Tính tích phân $I = \\int_{0}^{1} (2x + e^x) \\, dx$. Kết quả đúng là:',
        'Ta có nguyên hàm: $\\int (2x + e^x) dx = x^2 + e^x$. Do đó: $I = [x^2 + e^x]_0^1 = (1 + e) - (0 + 1) = e$.',
        'medium', 'multiple_choice',
        '[{"id": "A", "content": "$I = e$", "isCorrect": true, "position": 1}, {"id": "B", "content": "$I = e + 1$", "isCorrect": false, "position": 2}, {"id": "C", "content": "$I = e - 1$", "isCorrect": false, "position": 3}, {"id": "D", "content": "$I = 2e$", "isCorrect": false, "position": 4}]'::jsonb,
        '{"toan12", "tich-phan"}', 'active');

    -- 5. Cấu hình Quiz Chương 1
    INSERT INTO assessments (
        course_id, chapter_id, teacher_id, type, title, description,
        time_limit_minutes, passing_score, max_attempts, config, active
    ) VALUES (
        v_toan12_id, v_ch1_id, v_thay_duc, 'QUIZ',
        'Kiểm tra Đánh giá Chương 1: Ứng dụng đạo hàm khảo sát hàm số',
        'Đề kiểm tra ngẫu nhiên 3 câu hỏi trắc nghiệm kiểm tra độ hiểu bài sau khi xem xong video bài giảng.',
        15, 70.00, 5,
        '{"easyCount": 2, "mediumCount": 1, "hardCount": 0, "shuffleChoices": true}'::jsonb,
        TRUE
    ) ON CONFLICT DO NOTHING;

    -- 6. Ghi danh & Cấp Chứng chỉ số
    INSERT INTO enrollments (
        student_id, course_id, completed_lessons, total_lessons, progress_percent,
        final_exam_passed, completed_at, enrolled_at
    ) VALUES (
        v_nam_id, v_toan6_id, 2, 2, 100.00, TRUE, NOW() - INTERVAL '2 days', NOW() - INTERVAL '7 days'
    ) ON CONFLICT (student_id, course_id) DO UPDATE SET progress_percent = 100.00
    RETURNING id INTO v_enr_toan6;

    INSERT INTO certificates (
        enrollment_id, student_id, course_id, serial_number, student_name_snapshot,
        course_title_snapshot, final_exam_score, pdf_url, issued_at
    ) VALUES (
        v_enr_toan6, v_nam_id, v_toan6_id, 'BEE-2026-MATH06-9988', 'Nguyễn Văn Nam',
        'Toán 6 — Khởi Đầu Vững Chắc: Số Học & Hình Học Trực Quan',
        95.00, 'https://res.cloudinary.com/hohjixsz/image/upload/v1/certificates/sample.pdf',
        NOW() - INTERVAL '2 days'
    ) ON CONFLICT (enrollment_id) DO NOTHING;

    INSERT INTO enrollments (
        student_id, course_id, completed_lessons, total_lessons, progress_percent,
        final_exam_passed, enrolled_at
    ) VALUES (
        v_nam_id, v_toan12_id, 2, 6, 33.33, FALSE, NOW() - INTERVAL '3 days'
    ) ON CONFLICT (student_id, course_id) DO UPDATE SET progress_percent = 33.33;

    -- 7. Đơn hàng PayOS VietQR
    INSERT INTO orders (
        student_id, payment_ref, order_code, payment_method, status,
        total_amount_vnd, discount_amount_vnd, expires_at, paid_at, created_at
    ) VALUES (
        v_nam_id, 'PAY-100001', 100001, 'PAYOS', 'paid',
        249000, 50000, NOW() + INTERVAL '1 day', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'
    ) ON CONFLICT (payment_ref) DO UPDATE SET status = 'paid'
    RETURNING id INTO v_ord1_id;

    INSERT INTO order_items (
        order_id, course_id, teacher_id, course_title_snapshot, price_at_purchase,
        platform_fee_rate, platform_fee_vnd, teacher_amount_vnd, item_status
    ) VALUES (
        v_ord1_id, v_toan12_id, v_thay_duc,
        'Toán 12 — Chinh Phục Khảo Sát Hàm Số, Đạo Hàm & Tích Phân Ứng Dụng',
        249000, 20.00, 49800, 199200, 'paid'
    ) ON CONFLICT DO NOTHING;

    INSERT INTO orders (
        student_id, payment_ref, order_code, payment_method, status,
        total_amount_vnd, discount_amount_vnd, expires_at, created_at
    ) VALUES (
        v_nam_id, 'PAY-100002', 100002, 'PAYOS', 'pending',
        199000, 51000, NOW() + INTERVAL '15 minutes', NOW()
    ) ON CONFLICT (payment_ref) DO UPDATE SET status = 'pending'
    RETURNING id INTO v_ord2_id;

    INSERT INTO order_items (
        order_id, course_id, teacher_id, course_title_snapshot, price_at_purchase,
        platform_fee_rate, platform_fee_vnd, teacher_amount_vnd, item_status
    ) VALUES (
        v_ord2_id, v_vatly12_id, v_co_phan,
        'Vật Lý 12 — Nắm Trọn Điểm 9+ Dao Động Cơ & Sóng Cơ Học',
        199000, 20.00, 39800, 159200, 'pending'
    ) ON CONFLICT DO NOTHING;

    -- 8. Liên kết Phụ huynh - Học sinh
    INSERT INTO parent_student_links (
        parent_id, student_id, student_email, invite_token, status,
        consent_granted, invite_expires_at, linked_at
    ) VALUES (
        v_tuan_id, v_nam_id, 'hocsinh.nam@beeacademy.edu.vn', 'BEE999', 'active',
        TRUE, NOW() + INTERVAL '30 days', NOW() - INTERVAL '5 days'
    ) ON CONFLICT (invite_token) DO UPDATE SET status = 'active';

    INSERT INTO parent_student_links (
        parent_id, student_id, student_email, invite_token, status,
        consent_granted, invite_expires_at
    ) VALUES (
        v_tuan_id, v_mai_id, 'hocsinh.mai@beeacademy.edu.vn', 'BEE123', 'pending',
        FALSE, NOW() + INTERVAL '7 days'
    ) ON CONFLICT (invite_token) DO UPDATE SET status = 'pending';

    -- 9. Tài khoản Ngân hàng GV & Quyết toán
    INSERT INTO teacher_bank_accounts (
        teacher_id, bank_name, account_number_encrypted, account_number_masked,
        account_holder, branch, verify_status, otp_verified, verified_at
    ) VALUES (
        v_thay_duc, 'Ngân hàng Quân Đội (MBBank)',
        'ENC_GCM_0348756972_SECURE', '***56972',
        'HOANG MINH DUC', 'Chi nhánh Hà Nội',
        'verified', TRUE, NOW() - INTERVAL '10 days'
    ) ON CONFLICT (teacher_id) DO UPDATE SET verify_status = 'verified';

    INSERT INTO payout_periods (
        teacher_id, month_year, status, total_gross_vnd, total_platform_fee_vnd,
        total_teacher_vnd, transaction_count, created_at
    ) VALUES (
        v_thay_duc, '2026-10', 'pending', 1500000, 300000, 1200000, 6, NOW() - INTERVAL '2 days'
    ) ON CONFLICT (teacher_id, month_year) DO NOTHING;

    -- 10. Đánh giá Khóa học
    INSERT INTO course_reviews (course_id, student_id, rating, comment, created_at)
    VALUES 
        (v_toan12_id, v_nam_id, 5, 'Khóa học của Thầy Đức thực sự tuyệt vời! Bài giảng rất trực quan, công thức Toán LaTeX hiển thị rõ nét và bài tập bám sát đề thi THPT Quốc Gia.', NOW() - INTERVAL '2 days'),
        (v_toan12_id, v_mai_id, 5, 'Em đã xem hết bài học thử và đăng ký ngay. Thầy giải bài tập cực kỳ có tâm!', NOW() - INTERVAL '1 day')
    ON CONFLICT (course_id, student_id) DO NOTHING;

    -- 11. Khiếu nại Hỗ trợ & Thông báo
    INSERT INTO complaints (
        sender_id, sender_role, title, category, priority, status, last_activity_at
    ) VALUES (
        v_nam_id, 'student', 'Cần hỗ trợ về tài liệu bài giảng chuyên đề Tích phân',
        'course_content', 'medium', 'open', NOW()
    ) RETURNING id INTO v_comp_id;

    INSERT INTO complaint_messages (complaint_id, author_id, author_role, content)
    VALUES (v_comp_id, v_nam_id, 'student', 'Em chào thầy cô, cho em hỏi tài liệu PDF bài giảng số 2 có kèm thêm phần bài tập rèn luyện về nhà không ạ?');

    INSERT INTO notifications (recipient_id, title, message, type, action_url)
    VALUES 
        (v_nam_id, 'Chào mừng đến với BeeAcademy!', 'Tài khoản của bạn đã được kích hoạt thành công. Bắt đầu khám phá kho bài giảng ngay hôm nay.', 'general', '/courses'),
        (v_nam_id, 'Thanh toán thành công đơn hàng #100001', 'Bạn đã thanh toán thành công khóa học Toán 12 qua VietQR. Chúc bạn học tập tốt!', 'payment_success', '/learning/courses/' || v_toan12_id),
        (v_nam_id, 'Cấp chứng chỉ hoàn thành khóa học', 'Chúc mừng bạn đã hoàn thành khóa học Toán 6 và được cấp Chứng chỉ số BEE-2026-MATH06-9988!', 'course_update', '/learning/certificates');

END $$;

