package com.beeacademy.backend.application.admin.usecase;

import com.beeacademy.backend.application.admin.dto.AdminDashboardDto;
import com.beeacademy.backend.domain.admin.AdminAnalyticsQueryPort;
import com.beeacademy.backend.domain.admin.PlatformAnalytics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case xem Dashboard quản trị: GMV, phí nền tảng, phần giáo viên, thống kê người dùng/khóa học (UC38).
 */
@Service
@Transactional(readOnly = true)
public class GetAdminDashboardUseCase {

    private final AdminAnalyticsQueryPort analyticsQueryPort;

    public GetAdminDashboardUseCase(AdminAnalyticsQueryPort analyticsQueryPort) {
        this.analyticsQueryPort = analyticsQueryPort;
    }

    public AdminDashboardDto execute() {
        PlatformAnalytics data = analyticsQueryPort.getPlatformAnalytics();
        return new AdminDashboardDto(
                data.totalGmv(),
                data.platformFee(),
                data.teacherAmount(),
                data.totalUsers(),
                data.studentCount(),
                data.teacherCount(),
                data.parentCount(),
                data.adminCount(),
                data.activeUsers(),
                data.lockedUsers(),
                data.pendingTeacherApprovals(),
                data.totalCourses(),
                data.publishedCourses(),
                data.draftCourses(),
                data.pendingCourseApprovals(),
                data.rejectedCourses(),
                data.totalEnrollments(),
                data.openComplaints()
        );
    }
}
