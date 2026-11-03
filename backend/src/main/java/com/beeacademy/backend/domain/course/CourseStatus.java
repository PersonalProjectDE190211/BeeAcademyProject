package com.beeacademy.backend.domain.course;

/**
 * Trạng thái của Khóa học trong luồng kiểm duyệt (Course State Machine).
 */
public enum CourseStatus {
    /**
     * Bản nháp, giáo viên đang biên soạn nội dung.
     */
    DRAFT,

    /**
     * Giáo viên đã nộp để chờ Quản trị viên (Admin) kiểm duyệt (UC39).
     */
    PENDING_REVIEW,

    /**
     * Admin đã phê duyệt và khóa học được hiển thị công khai trên sàn học.
     */
    PUBLISHED,

    /**
     * Admin yêu cầu giáo viên chỉnh sửa/bổ sung nội dung kèm lý do phản hồi.
     */
    NEEDS_REVISION,

    /**
     * Admin từ chối khóa học (vi phạm chính sách, nội dung không đạt chuẩn).
     */
    REJECTED,

    /**
     * Khóa học bị lưu trữ hoặc tạm ngừng kinh doanh.
     */
    ARCHIVED
}

