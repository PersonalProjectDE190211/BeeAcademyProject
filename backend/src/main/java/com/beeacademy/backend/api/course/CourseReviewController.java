package com.beeacademy.backend.api.course;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.api.course.dto.SubmitReviewRequest;
import com.beeacademy.backend.application.course.dto.CourseReviewResult;
import com.beeacademy.backend.application.course.dto.SubmitReviewCommand;
import com.beeacademy.backend.application.course.usecase.ManageCourseReviewUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller gửi đánh giá nhận xét khóa học dành cho Học viên (UC08).
 */
@Tag(name = "6. Đánh giá Khóa học (Review)", description = "Các API gửi đánh giá sao và bình luận nhận xét của học viên")
@RestController
@RequestMapping("/api/courses/{courseId}/reviews")
public class CourseReviewController {

    private final ManageCourseReviewUseCase manageCourseReviewUseCase;

    public CourseReviewController(ManageCourseReviewUseCase manageCourseReviewUseCase) {
        this.manageCourseReviewUseCase = manageCourseReviewUseCase;
    }

    /**
     * Gửi hoặc cập nhật đánh giá nhận xét của học viên cho khóa học.
     */
    @Operation(summary = "Gửi đánh giá khóa học", description = "Đánh giá từ 1 đến 5 sao và bình luận nhận xét")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseReviewResult>> submitReview(
            @PathVariable UUID courseId,
            @Valid @RequestBody SubmitReviewRequest request
    ) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        SubmitReviewCommand command = new SubmitReviewCommand(
                courseId,
                studentId,
                request.rating(),
                request.comment()
        );
        CourseReviewResult review = manageCourseReviewUseCase.submitReview(command);
        return ResponseEntity.ok(ApiResponse.success("Đánh giá khóa học thành công", review));
    }
}
