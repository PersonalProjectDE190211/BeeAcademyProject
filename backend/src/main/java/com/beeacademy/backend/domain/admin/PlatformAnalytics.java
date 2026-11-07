package com.beeacademy.backend.domain.admin;

/**
 * Read model tổng hợp số liệu toàn nền tảng phục vụ Dashboard quản trị (UC38).
 * Bất biến tài chính: totalGmv = platformFee + teacherAmount.
 */
public record PlatformAnalytics(
        long totalGmv,
        long platformFee,
        long teacherAmount,
        long totalUsers,
        long studentCount,
        long teacherCount,
        long parentCount,
        long adminCount,
        long activeUsers,
        long lockedUsers,
        long pendingTeacherApprovals,
        long totalCourses,
        long publishedCourses,
        long draftCourses,
        long pendingCourseApprovals,
        long rejectedCourses,
        long totalEnrollments,
        long openComplaints
) {
}

