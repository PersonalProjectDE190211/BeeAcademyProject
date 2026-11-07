package com.beeacademy.backend.infrastructure.admin.query;

import com.beeacademy.backend.domain.admin.AdminAnalyticsQueryPort;
import com.beeacademy.backend.domain.admin.PlatformAnalytics;
import com.beeacademy.backend.infrastructure.auth.persistence.repository.SpringDataProfileRepository;
import com.beeacademy.backend.infrastructure.commerce.persistence.repository.SpringDataRevenueSplitRepository;
import com.beeacademy.backend.infrastructure.course.persistence.repository.SpringDataCourseRepository;
import com.beeacademy.backend.infrastructure.learning.persistence.repository.SpringDataEnrollmentRepository;
import com.beeacademy.backend.infrastructure.support.persistence.repository.SpringDataComplaintRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter thực thi {@link AdminAnalyticsQueryPort} bằng các truy vấn đếm/tổng hợp trên Spring Data JPA.
 */
@Component
@Transactional(readOnly = true)
public class AdminAnalyticsQueryAdapter implements AdminAnalyticsQueryPort {

    private final SpringDataRevenueSplitRepository revenueSplitRepository;
    private final SpringDataProfileRepository profileRepository;
    private final SpringDataCourseRepository courseRepository;
    private final SpringDataEnrollmentRepository enrollmentRepository;
    private final SpringDataComplaintRepository complaintRepository;

    public AdminAnalyticsQueryAdapter(
            SpringDataRevenueSplitRepository revenueSplitRepository,
            SpringDataProfileRepository profileRepository,
            SpringDataCourseRepository courseRepository,
            SpringDataEnrollmentRepository enrollmentRepository,
            SpringDataComplaintRepository complaintRepository
    ) {
        this.revenueSplitRepository = revenueSplitRepository;
        this.profileRepository = profileRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.complaintRepository = complaintRepository;
    }

    @Override
    public PlatformAnalytics getPlatformAnalytics() {
        return new PlatformAnalytics(
                revenueSplitRepository.sumTotalGrossAmount(),
                revenueSplitRepository.sumTotalPlatformFee(),
                revenueSplitRepository.sumTotalTeacherAmount(),
                profileRepository.count(),
                profileRepository.countByRole("student"),
                profileRepository.countByRole("teacher"),
                profileRepository.countByRole("parent"),
                profileRepository.countByRole("admin"),
                profileRepository.countByStatus("active"),
                profileRepository.countByStatus("locked") + profileRepository.countByStatus("suspended"),
                profileRepository.countByTeacherApprovalStatus("pending"),
                courseRepository.count(),
                courseRepository.countByStatus("published"),
                courseRepository.countByStatus("draft"),
                courseRepository.countByStatus("pending_approval"),
                courseRepository.countByStatus("rejected"),
                enrollmentRepository.count(),
                complaintRepository.countByStatus("open") + complaintRepository.countByStatus("in_progress")
        );
    }
}
