package com.beeacademy.backend.domain.ai;

/**
 * Các loại ý định người dùng khi trò chuyện với Trợ lý BeeBot.
 */
public enum ChatbotIntent {
    SEARCH_COURSE,          // Tìm khóa học theo môn, khối lớp, giá
    COURSE_DETAIL,          // Hỏi chi tiết một khóa học cụ thể
    COURSE_RECOMMENDATION,  // Nhờ tư vấn lộ trình học hoặc khóa học phù hợp mục tiêu
    PRICE_INQUIRY,          // Hỏi về học phí, hoàn tiền hoặc phương thức thanh toán
    GENERAL_QUESTION,       // Câu hỏi chung về BeeAcademy, tuyển sinh, chính sách
    CLARIFICATION,          // Cần người dùng làm rõ thêm thông tin
    CORRECT_SEARCH;         // Người dùng sửa đổi/đính chính điều kiện tìm kiếm trước đó

    public static ChatbotIntent fromString(String val) {
        if (val == null || val.isBlank()) {
            return GENERAL_QUESTION;
        }
        try {
            return ChatbotIntent.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GENERAL_QUESTION;
        }
    }
}

