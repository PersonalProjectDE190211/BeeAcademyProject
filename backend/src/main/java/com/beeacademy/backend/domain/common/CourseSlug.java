package com.beeacademy.backend.domain.common;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Value Object biểu diễn đường dẫn thân thiện (URL-friendly Slug) cho Khóa học.
 * Đóng gói thuật toán chuẩn hóa tiếng Việt có dấu sang không dấu và ký tự an toàn trên URL.
 */
public record CourseSlug(String value) implements Serializable {

    public CourseSlug {
        if (value == null || value.isBlank()) {
            throw new DomainException("INVALID_SLUG", "Slug không được để trống");
        }
    }

    public static CourseSlug of(String value) {
        return new CourseSlug(value);
    }

    /**
     * Tạo CourseSlug từ tiêu đề khóa học tiếng Việt.
     * Ví dụ: "Toán Lớp 8: Đa thức & Hằng đẳng thức" -> "toan-lop-8-da-thuc-hang-dang-thuc"
     */
    public static CourseSlug fromTitle(String title) {
        if (title == null || title.isBlank()) {
            return new CourseSlug("course");
        }

        String s = title.trim().toLowerCase(Locale.ROOT);
        // Xử lý riêng ký tự đ/Đ vì NFD không phân rã được chữ này
        s = s.replace("đ", "d").replace("Đ", "d");

        // Phân rã dấu thanh tiếng Việt (NFD) và xóa combining marks
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Chỉ giữ lại chữ cái thường a-z, chữ số 0-9, khoảng trắng và dấu gạch ngang
        s = s.replaceAll("[^a-z0-9\\s-]", "");

        // Thay khoảng trắng thành dấu gạch ngang, gộp nhiều dấu gạch ngang liền nhau
        s = s.replaceAll("\\s+", "-");
        s = s.replaceAll("-+", "-");

        // Xóa dấu gạch ngang ở đầu và cuối
        s = s.replaceAll("^-|-$", "");

        if (s.isBlank()) {
            s = "course";
        }

        return new CourseSlug(s);
    }

    @Override
    public String toString() {
        return this.value;
    }
}

