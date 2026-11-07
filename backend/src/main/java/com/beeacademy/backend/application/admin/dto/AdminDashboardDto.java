package com.beeacademy.backend.application.admin.dto;

public record AdminDashboardDto(
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

