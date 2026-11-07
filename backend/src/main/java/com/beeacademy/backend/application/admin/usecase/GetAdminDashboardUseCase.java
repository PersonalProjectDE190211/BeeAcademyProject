package com.beeacademy.backend.application.admin.usecase;

import com.beeacademy.backend.application.admin.dto.AdminDashboardDto;
import com.beeacademy.backend.infrastructure.admin.query.AdminAnalyticsQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetAdminDashboardUseCase {

    private final AdminAnalyticsQueryService analyticsQueryService;

    public GetAdminDashboardUseCase(AdminAnalyticsQueryService analyticsQueryService) {
        this.analyticsQueryService = analyticsQueryService;
    }

    public AdminDashboardDto execute() {
        AdminAnalyticsQueryService.AdminAnalyticsData data = analyticsQueryService.getPlatformAnalytics();
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

