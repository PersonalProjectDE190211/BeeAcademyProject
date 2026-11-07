package com.beeacademy.backend.infrastructure.admin.query;

import com.beeacademy.backend.infrastructure.auth.persistence.repository.SpringDataProfileRepository;
import com.beeacademy.backend.infrastructure.commerce.persistence.repository.SpringDataRevenueSplitRepository;
import com.beeacademy.backend.infrastructure.course.persistence.repository.SpringDataCourseRepository;
import com.beeacademy.backend.infrastructure.learning.persistence.repository.SpringDataEnrollmentRepository;
import com.beeacademy.backend.infrastructure.support.persistence.repository.SpringDataComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminAnalyticsQueryService {

    private final SpringDataRevenueSplitRepository revenueSplitRepository;
    private final SpringDataProfileRepository profileRepository;
    private final SpringDataCourseRepository courseRepository;
    private final SpringDataEnrollmentRepository enrollmentRepository;
    private final SpringDataComplaintRepository complaintRepository;

    public AdminAnalyticsQueryService(
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

    public AdminAnalyticsData getPlatformAnalytics() {
        long totalGmv = revenueSplitRepository.sumTotalGrossAmount();
        long platformFee = revenueSplitRepository.sumTotalPlatformFee();
        long teacherAmount = revenueSplitRepository.sumTotalTeacherAmount();

        long totalUsers = profileRepository.count();
        long studentCount = profileRepository.countByRole("student");
        long teacherCount = profileRepository.countByRole("teacher");
        long parentCount = profileRepository.countByRole("parent");
        long adminCount = profileRepository.countByRole("admin");

        long activeUsers = profileRepository.countByStatus("active");
        long lockedUsers = profileRepository.countByStatus("locked") + profileRepository.countByStatus("suspended");
        long pendingTeacherApprovals = profileRepository.countByTeacherApprovalStatus("pending");

        long totalCourses = courseRepository.count();
        long publishedCourses = courseRepository.countByStatus("published");
        long draftCourses = courseRepository.countByStatus("draft");
        long pendingCourseApprovals = courseRepository.countByStatus("pending_approval");
        long rejectedCourses = courseRepository.countByStatus("rejected");

        long totalEnrollments = enrollmentRepository.count();
        long openComplaints = complaintRepository.countByStatus("open") + complaintRepository.countByStatus("in_progress");

        return new AdminAnalyticsData(
                totalGmv,
                platformFee,
                teacherAmount,
                totalUsers,
                studentCount,
                teacherCount,
                parentCount,
                adminCount,
                activeUsers,
                lockedUsers,
                pendingTeacherApprovals,
                totalCourses,
                publishedCourses,
                draftCourses,
                pendingCourseApprovals,
                rejectedCourses,
                totalEnrollments,
                openComplaints
        );
    }

    public record AdminAnalyticsData(
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
    ) {}
}

