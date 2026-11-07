package com.beeacademy.backend.api.family;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.api.family.dto.InviteStudentRequest;
import com.beeacademy.backend.api.family.dto.UnlinkRequest;
import com.beeacademy.backend.application.family.dto.InviteStudentCommand;
import com.beeacademy.backend.application.family.dto.ParentLinkResult;
import com.beeacademy.backend.application.family.dto.StudentProgressReportResult;
import com.beeacademy.backend.application.family.dto.UnlinkCommand;
import com.beeacademy.backend.application.family.usecase.ManageParentLinkUseCase;
import com.beeacademy.backend.application.family.usecase.ParentViewStudentProgressUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "18. Cổng Phụ huynh (Parent Portal)", description = "Các API mời con liên kết tài khoản, xem danh sách con đã liên kết, theo dõi tiến độ học tập và hủy liên kết")
@RestController
@RequestMapping("/api/parent")
@PreAuthorize("hasRole('PARENT')")
public class ParentPortalController {

    private final ManageParentLinkUseCase manageParentLinkUseCase;
    private final ParentViewStudentProgressUseCase parentViewStudentProgressUseCase;

    public ParentPortalController(
            ManageParentLinkUseCase manageParentLinkUseCase,
            ParentViewStudentProgressUseCase parentViewStudentProgressUseCase
    ) {
        this.manageParentLinkUseCase = manageParentLinkUseCase;
        this.parentViewStudentProgressUseCase = parentViewStudentProgressUseCase;
    }

    @Operation(summary = "Gửi lời mời liên kết cho con bằng Email", description = "Tạo lời mời liên kết mới với mã bảo mật 6 ký tự gửi đến học sinh (hiệu lực 72 giờ)")
    @PostMapping("/links/invite")
    public ResponseEntity<ApiResponse<ParentLinkResult>> inviteStudent(@Valid @RequestBody InviteStudentRequest request) {
        UUID parentId = SecurityUtils.requireCurrentUserId();
        ParentLinkResult result = manageParentLinkUseCase.inviteStudent(
                new InviteStudentCommand(parentId, request.studentEmail())
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Gửi lời mời liên kết thành công", result));
    }

    @Operation(summary = "Xem danh sách các mối liên kết với con", description = "Danh sách học sinh đã liên kết hoặc đang chờ chấp thuận của phụ huynh hiện tại")
    @GetMapping("/links")
    public ResponseEntity<ApiResponse<List<ParentLinkResult>>> getMyLinks() {
        UUID parentId = SecurityUtils.requireCurrentUserId();
        List<ParentLinkResult> results = manageParentLinkUseCase.getParentLinks(parentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách liên kết thành công", results));
    }

    @Operation(summary = "Xem tiến độ học tập của con", description = "Truy xuất danh sách khóa học con đang học, tỷ lệ hoàn thành (tự động ghi vết kiểm toán Privacy Audit Log)")
    @GetMapping("/students/{studentId}/progress")
    public ResponseEntity<ApiResponse<StudentProgressReportResult>> getStudentProgress(@PathVariable UUID studentId) {
        UUID parentId = SecurityUtils.requireCurrentUserId();
        StudentProgressReportResult result = parentViewStudentProgressUseCase.execute(parentId, studentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo tiến độ học tập thành công", result));
    }

    @Operation(summary = "Phụ huynh hủy liên kết với con", description = "Chấm dứt mối liên kết và thu hồi quyền xem tiến độ học tập ngay lập tức")
    @PostMapping("/links/{linkId}/unlink")
    public ResponseEntity<ApiResponse<ParentLinkResult>> unlinkStudent(
            @PathVariable UUID linkId,
            @RequestBody(required = false) UnlinkRequest request
    ) {
        UUID parentId = SecurityUtils.requireCurrentUserId();
        String reason = request != null ? request.reason() : "Phụ huynh chủ động hủy liên kết";
        ParentLinkResult result = manageParentLinkUseCase.unlink(new UnlinkCommand(parentId, linkId, reason));
        return ResponseEntity.ok(ApiResponse.success("Hủy liên kết thành công", result));
    }
}

