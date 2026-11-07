package com.beeacademy.backend.infrastructure.admin;

import com.beeacademy.backend.infrastructure.admin.query.AdminAnalyticsQueryService;
import com.beeacademy.backend.infrastructure.auth.persistence.repository.SpringDataProfileRepository;
import com.beeacademy.backend.infrastructure.commerce.persistence.repository.SpringDataRevenueSplitRepository;
import com.beeacademy.backend.infrastructure.course.persistence.repository.SpringDataCourseRepository;
import com.beeacademy.backend.infrastructure.learning.persistence.repository.SpringDataEnrollmentRepository;
import com.beeacademy.backend.infrastructure.support.persistence.repository.SpringDataComplaintRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsQueryServiceTest {

    @Mock
    private SpringDataRevenueSplitRepository revenueSplitRepository;

    @Mock
    private SpringDataProfileRepository profileRepository;

    @Mock
    private SpringDataCourseRepository courseRepository;

    @Mock
    private SpringDataEnrollmentRepository enrollmentRepository;

    @Mock
    private SpringDataComplaintRepository complaintRepository;

    @InjectMocks
    private AdminAnalyticsQueryService queryService;

    @Test
    @DisplayName("getPlatformAnalytics tổng hợp chính xác các chỉ số GMV, User, Course và Hỗ trợ")
    void shouldAggregatePlatformAnalyticsCorrectly() {
        when(revenueSplitRepository.sumTotalGrossAmount()).thenReturn(10_000_000L);
        when(revenueSplitRepository.sumTotalPlatformFee()).thenReturn(2_000_000L);
        when(revenueSplitRepository.sumTotalTeacherAmount()).thenReturn(8_000_000L);

        when(profileRepository.count()).thenReturn(100L);
        when(profileRepository.countByRole("student")).thenReturn(70L);
        when(profileRepository.countByRole("teacher")).thenReturn(20L);
        when(profileRepository.countByRole("parent")).thenReturn(8L);
        when(profileRepository.countByRole("admin")).thenReturn(2L);
        when(profileRepository.countByStatus("active")).thenReturn(95L);
        when(profileRepository.countByStatus("locked")).thenReturn(3L);
        when(profileRepository.countByStatus("suspended")).thenReturn(2L);
        when(profileRepository.countByTeacherApprovalStatus("pending")).thenReturn(4L);

        when(courseRepository.count()).thenReturn(30L);
        when(courseRepository.countByStatus("published")).thenReturn(25L);
        when(courseRepository.countByStatus("draft")).thenReturn(3L);
        when(courseRepository.countByStatus("pending_approval")).thenReturn(2L);
        when(courseRepository.countByStatus("rejected")).thenReturn(0L);

        when(enrollmentRepository.count()).thenReturn(150L);
        when(complaintRepository.countByStatus("open")).thenReturn(3L);
        when(complaintRepository.countByStatus("in_progress")).thenReturn(1L);

        AdminAnalyticsQueryService.AdminAnalyticsData data = queryService.getPlatformAnalytics();

        assertThat(data.totalGmv()).isEqualTo(10_000_000L);
        assertThat(data.platformFee()).isEqualTo(2_000_000L);
        assertThat(data.teacherAmount()).isEqualTo(8_000_000L);
        assertThat(data.totalUsers()).isEqualTo(100L);
        assertThat(data.studentCount()).isEqualTo(70L);
        assertThat(data.teacherCount()).isEqualTo(20L);
        assertThat(data.activeUsers()).isEqualTo(95L);
        assertThat(data.lockedUsers()).isEqualTo(5L);
        assertThat(data.pendingTeacherApprovals()).isEqualTo(4L);
        assertThat(data.totalCourses()).isEqualTo(30L);
        assertThat(data.publishedCourses()).isEqualTo(25L);
        assertThat(data.pendingCourseApprovals()).isEqualTo(2L);
        assertThat(data.totalEnrollments()).isEqualTo(150L);
        assertThat(data.openComplaints()).isEqualTo(4L);
    }
}

