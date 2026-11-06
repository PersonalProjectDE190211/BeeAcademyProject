package com.beeacademy.backend.api.course;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.api.course.dto.AddChapterRequest;
import com.beeacademy.backend.api.course.dto.AddLessonRequest;
import com.beeacademy.backend.api.course.dto.CreateCourseRequest;
import com.beeacademy.backend.api.course.dto.UpdateCourseRequest;
import com.beeacademy.backend.application.course.dto.*;
import com.beeacademy.backend.application.course.usecase.ManageTeacherCourseUseCase;
import com.beeacademy.backend.application.course.usecase.SubmitCourseForReviewUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller dành cho Giáo viên quản lý Giáo trình & Khóa học (UC30, UC31, UC39):
 * - Tạo và cập nhật khóa học
 * - Thêm, sửa, xóa chương và bài giảng
 * - Nộp duyệt khóa học sang Admin
 */
@Tag(name = "4. Quản lý Khóa học Giáo viên (Teacher)", description = "Các API tạo giáo trình, bài giảng và nộp duyệt khóa học")
@RestController
@RequestMapping("/api/teacher/courses")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherCourseController {

    private final ManageTeacherCourseUseCase manageTeacherCourseUseCase;
    private final SubmitCourseForReviewUseCase submitCourseForReviewUseCase;

    public TeacherCourseController(ManageTeacherCourseUseCase manageTeacherCourseUseCase,
                                   SubmitCourseForReviewUseCase submitCourseForReviewUseCase) {
        this.manageTeacherCourseUseCase = manageTeacherCourseUseCase;
        this.submitCourseForReviewUseCase = submitCourseForReviewUseCase;
    }

    /**
     * Lấy danh sách khóa học của giáo viên đang đăng nhập.
     */
    @Operation(summary = "Danh sách khóa học của tôi")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseSummaryResult>>> getMyCourses() {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        List<CourseSummaryResult> courses = manageTeacherCourseUseCase.getTeacherCourses(teacherId);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    /**
     * Xem chi tiết khóa học của tôi (bao gồm video URL đầy đủ).
     */
    @Operation(summary = "Xem chi tiết khóa học của giáo viên")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailResult>> getMyCourseDetail(@PathVariable UUID id) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        CourseDetailResult detail = manageTeacherCourseUseCase.getTeacherCourseDetail(id, teacherId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * UC30: Tạo khóa học mới ở trạng thái Nháp (DRAFT).
     */
    @Operation(summary = "Tạo khóa học mới (DRAFT)")
    @PostMapping
    public ResponseEntity<ApiResponse<CourseDetailResult>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        CreateCourseCommand command = new CreateCourseCommand(
                teacherId,
                request.categoryId(),
                request.title(),
                request.description(),
                request.thumbnailUrl(),
                request.priceVnd(),
                request.grades()
        );
        CourseDetailResult course = manageTeacherCourseUseCase.createCourse(command);
        return ResponseEntity.ok(ApiResponse.success("Tạo khóa học thành công", course));
    }

    /**
     * Cập nhật thông tin cơ bản & học phí của khóa học.
     */
    @Operation(summary = "Cập nhật thông tin cơ bản & học phí")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailResult>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseRequest request
    ) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        UpdateCourseCommand command = new UpdateCourseCommand(
                id,
                teacherId,
                request.categoryId(),
                request.title(),
                request.description(),
                request.thumbnailUrl(),
                request.priceVnd(),
                request.grades()
        );
        CourseDetailResult course = manageTeacherCourseUseCase.updateCourse(command);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khóa học thành công", course));
    }

    /**
     * Thêm chương học mới vào khóa học.
     */
    @Operation(summary = "Thêm chương học")
    @PostMapping("/{id}/chapters")
    public ResponseEntity<ApiResponse<CourseDetailResult>> addChapter(
            @PathVariable UUID id,
            @Valid @RequestBody AddChapterRequest request
    ) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        AddChapterCommand command = new AddChapterCommand(id, teacherId, request.title(), request.description());
        CourseDetailResult course = manageTeacherCourseUseCase.addChapter(command);
        return ResponseEntity.ok(ApiResponse.success("Thêm chương học thành công", course));
    }

    /**
     * Xóa chương học khỏi khóa học.
     */
    @Operation(summary = "Xóa chương học")
    @DeleteMapping("/{id}/chapters/{chapterId}")
    public ResponseEntity<ApiResponse<CourseDetailResult>> deleteChapter(
            @PathVariable UUID id,
            @PathVariable UUID chapterId
    ) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        CourseDetailResult course = manageTeacherCourseUseCase.deleteChapter(id, teacherId, chapterId);
        return ResponseEntity.ok(ApiResponse.success("Xóa chương học thành công", course));
    }

    /**
     * UC31: Thêm bài giảng vào một chương học.
     */
    @Operation(summary = "Thêm bài giảng vào chương")
    @PostMapping("/{id}/chapters/{chapterId}/lessons")
    public ResponseEntity<ApiResponse<CourseDetailResult>> addLesson(
            @PathVariable UUID id,
            @PathVariable UUID chapterId,
            @Valid @RequestBody AddLessonRequest request
    ) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        AddLessonCommand command = new AddLessonCommand(
                id,
                teacherId,
                chapterId,
                request.title(),
                request.videoUrl(),
                request.durationSec(),
                request.isFreePreview()
        );
        CourseDetailResult course = manageTeacherCourseUseCase.addLesson(command);
        return ResponseEntity.ok(ApiResponse.success("Thêm bài giảng thành công", course));
    }

    /**
     * Xóa bài giảng khỏi chương học.
     */
    @Operation(summary = "Xóa bài giảng")
    @DeleteMapping("/{id}/chapters/{chapterId}/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<CourseDetailResult>> deleteLesson(
            @PathVariable UUID id,
            @PathVariable UUID chapterId,
            @PathVariable UUID lessonId
    ) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        CourseDetailResult course = manageTeacherCourseUseCase.deleteLesson(id, teacherId, chapterId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Xóa bài giảng thành công", course));
    }

    /**
     * UC30, UC39: Nộp khóa học để Admin kiểm duyệt (chuyển sang PENDING_REVIEW).
     */
    @Operation(summary = "Nộp khóa học để Admin phê duyệt")
    @PostMapping("/{id}/submit-review")
    public ResponseEntity<ApiResponse<CourseDetailResult>> submitForReview(@PathVariable UUID id) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        CourseDetailResult course = submitCourseForReviewUseCase.execute(id, teacherId);
        return ResponseEntity.ok(ApiResponse.success("Đã nộp khóa học chờ Admin phê duyệt", course));
    }
}
