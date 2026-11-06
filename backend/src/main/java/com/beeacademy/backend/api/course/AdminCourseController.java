package com.beeacademy.backend.api.course;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.api.course.dto.ReviewCourseDecisionRequest;
import com.beeacademy.backend.application.course.dto.CourseDetailResult;
import com.beeacademy.backend.application.course.dto.CourseSummaryResult;
import com.beeacademy.backend.application.course.usecase.ReviewCourseApprovalUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller dành cho Quản trị viên kiểm duyệt Khóa học (UC41):
 * - Xem danh sách khóa học chờ duyệt (PENDING_REVIEW)
 * - Phê duyệt khóa học (PUBLISHED)
 * - Từ chối phê duyệt (REJECTED)
 * - Yêu cầu giáo viên sửa đổi (NEEDS_REVISION)
 */
@Tag(name = "5. Kiểm duyệt Khóa học Quản trị viên (Admin)", description = "Các API thẩm định nội dung, phê duyệt hoặc từ chối khóa học")
@RestController
@RequestMapping("/api/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    private final ReviewCourseApprovalUseCase reviewCourseApprovalUseCase;

    public AdminCourseController(ReviewCourseApprovalUseCase reviewCourseApprovalUseCase) {
        this.reviewCourseApprovalUseCase = reviewCourseApprovalUseCase;
    }

    /**
     * Lấy danh sách khóa học đang chờ Admin kiểm duyệt.
     */
    @Operation(summary = "Danh sách khóa học chờ duyệt")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<CourseSummaryResult>>> getPendingCourses() {
        List<CourseSummaryResult> courses = reviewCourseApprovalUseCase.getPendingCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    /**
     * Phê duyệt xuất bản khóa học lên hệ thống (PUBLISHED).
     */
    @Operation(summary = "Phê duyệt khóa học (PUBLISHED)")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<CourseDetailResult>> approveCourse(
            @PathVariable UUID id,
            @RequestBody(required = false) ReviewCourseDecisionRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        String comment = request != null ? request.reasonOrComment() : null;
        CourseDetailResult course = reviewCourseApprovalUseCase.approve(id, adminId, comment);
        return ResponseEntity.ok(ApiResponse.success("Khóa học đã được phê duyệt và xuất bản thành công", course));
    }

    /**
     * Từ chối phê duyệt khóa học (REJECTED).
     */
    @Operation(summary = "Từ chối khóa học (REJECTED)")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<CourseDetailResult>> rejectCourse(
            @PathVariable UUID id,
            @RequestBody(required = false) ReviewCourseDecisionRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        String reason = request != null ? request.reasonOrComment() : "Không đạt tiêu chuẩn kiểm duyệt nội dung";
        CourseDetailResult course = reviewCourseApprovalUseCase.reject(id, adminId, reason);
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối phê duyệt khóa học", course));
    }

    /**
     * Yêu cầu giáo viên bổ sung / chỉnh sửa khóa học (NEEDS_REVISION).
     */
    @Operation(summary = "Yêu cầu chỉnh sửa bổ sung (NEEDS_REVISION)")
    @PostMapping("/{id}/request-revision")
    public ResponseEntity<ApiResponse<CourseDetailResult>> requestRevision(
            @PathVariable UUID id,
            @RequestBody(required = false) ReviewCourseDecisionRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        String feedback = request != null ? request.reasonOrComment() : "Cần bổ sung thêm tài liệu và mô tả bài giảng";
        CourseDetailResult course = reviewCourseApprovalUseCase.requestRevision(id, adminId, feedback);
        return ResponseEntity.ok(ApiResponse.success("Đã gửi yêu cầu chỉnh sửa cho giáo viên", course));
    }
}

