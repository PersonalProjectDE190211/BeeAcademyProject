package com.beeacademy.backend.application.admin;

import com.beeacademy.backend.application.admin.dto.AdminDashboardDto;
import com.beeacademy.backend.application.admin.usecase.GetAdminDashboardUseCase;
import com.beeacademy.backend.domain.admin.AdminAnalyticsQueryPort;
import com.beeacademy.backend.domain.admin.PlatformAnalytics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAdminDashboardUseCaseTest {

    @Mock
    private AdminAnalyticsQueryPort analyticsQueryService;

    @InjectMocks
    private GetAdminDashboardUseCase useCase;

    @Test
    @DisplayName("execute trả về AdminDashboardDto đầy đủ các trường thống kê")
    void shouldReturnDashboardDto() {
        PlatformAnalytics data = new PlatformAnalytics(
                15_000_000L,
                3_000_000L,
                12_000_000L,
                120L,
                80L,
                25L,
                13L,
                2L,
                115L,
                5L,
                3L,
                40L,
                30L,
                5L,
                4L,
                1L,
                200L,
                6L
        );
        when(analyticsQueryService.getPlatformAnalytics()).thenReturn(data);

        AdminDashboardDto dto = useCase.execute();

        assertThat(dto.totalGmv()).isEqualTo(15_000_000L);
        assertThat(dto.platformFee()).isEqualTo(3_000_000L);
        assertThat(dto.teacherAmount()).isEqualTo(12_000_000L);
        assertThat(dto.totalUsers()).isEqualTo(120L);
        assertThat(dto.studentCount()).isEqualTo(80L);
        assertThat(dto.teacherCount()).isEqualTo(25L);
        assertThat(dto.activeUsers()).isEqualTo(115L);
        assertThat(dto.lockedUsers()).isEqualTo(5L);
        assertThat(dto.pendingTeacherApprovals()).isEqualTo(3L);
        assertThat(dto.totalCourses()).isEqualTo(40L);
        assertThat(dto.publishedCourses()).isEqualTo(30L);
        assertThat(dto.totalEnrollments()).isEqualTo(200L);
        assertThat(dto.openComplaints()).isEqualTo(6L);
    }
}

