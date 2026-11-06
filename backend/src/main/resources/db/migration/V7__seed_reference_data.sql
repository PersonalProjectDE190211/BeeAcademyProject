-- ============================================================================
-- V7: Khởi tạo Dữ liệu nền tảng mặc định (Seed Reference Data)
-- ============================================================================

-- 1. Cấu hình hệ thống mặc định
INSERT INTO system_settings (key, value, description)
VALUES
    ('maintenance_mode', '{"enabled": false, "message": "Hệ thống BeeAcademy đang bảo trì nâng cấp định kỳ. Vui lòng quay lại sau!"}'::jsonb, 'Cấu hình bật/tắt chế độ bảo trì toàn hệ thống'),
    ('revenue_split_policy', '{"platformFeeRate": 20.0, "teacherShareRate": 80.0}'::jsonb, 'Chính sách phân chia doanh thu khóa học (80% Giáo viên / 20% Nền tảng)')
ON CONFLICT (key) DO NOTHING;

-- 2. Danh mục môn học chuẩn (Categories)
INSERT INTO categories (name, slug, description, icon_name, display_order, active)
VALUES
    ('Toán học', 'toan-hoc', 'Đại số, Hình học và Tư duy Toán học từ lớp 1 đến lớp 12', 'Calculator', 1, TRUE),
    ('Tin học & Lập trình', 'tin-hoc-lap-trinh', 'Khoa học máy tính, Thuật toán, Python, Web và AI cho học sinh', 'Code', 2, TRUE),
    ('Tiếng Anh', 'tieng-anh', 'Ngữ pháp, Từ vựng, Giao tiếp và Luyện thi chứng chỉ quốc tế', 'Globe', 3, TRUE),
    ('Khoa học tự nhiên', 'khoa-hoc-tu-nhien', 'Vật lý, Hóa học và Sinh học ứng dụng thực tiễn', 'Atom', 4, TRUE),
    ('Ngữ văn', 'ngu-van', 'Đọc hiểu văn bản, Nghị luận xã hội và Nghị luận văn học', 'BookOpen', 5, TRUE)
ON CONFLICT (slug) DO NOTHING;
