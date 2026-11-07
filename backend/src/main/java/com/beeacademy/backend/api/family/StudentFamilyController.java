package com.beeacademy.backend.api.family;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.api.family.dto.RespondLinkRequest;
import com.beeacademy.backend.api.family.dto.UnlinkRequest;
import com.beeacademy.backend.application.family.dto.AcceptLinkCommand;
import com.beeacademy.backend.application.family.dto.ParentLinkResult;
import com.beeacademy.backend.application.family.dto.RejectLinkCommand;
import com.beeacademy.backend.application.family.dto.UnlinkCommand;
import com.beeacademy.backend.application.family.usecase.ManageParentLinkUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "19. Gia đình của Học viên (Student Family Links)", description = "Các API học sinh tiếp nhận lời mời liên kết của phụ huynh, chấp thuận cấp quyền theo dõi hoặc hủy liên kết")
@RestController
@RequestMapping("/api/student/family")
@PreAuthorize("hasRole('STUDENT')")
public class StudentFamilyController {

    private final ManageParentLinkUseCase manageParentLinkUseCase;

    public StudentFamilyController(ManageParentLinkUseCase manageParentLinkUseCase) {
        this.manageParentLinkUseCase = manageParentLinkUseCase;
    }

    @Operation(summary = "Học sinh chấp nhận lời mời liên kết", description = "Nhập mã mời 6 ký tự để kích hoạt liên kết và đồng thuận chia sẻ tiến độ học tập với phụ huynh")
    @PostMapping("/links/accept")
    public ResponseEntity<ApiResponse<ParentLinkResult>> acceptInvite(@Valid @RequestBody RespondLinkRequest request) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        ParentLinkResult result = manageParentLinkUseCase.acceptInvite(
                new AcceptLinkCommand(studentId, request.inviteToken())
        );
        return ResponseEntity.ok(ApiResponse.success("Chấp thuận liên kết thành công", result));
    }

    @Operation(summary = "Học sinh từ chối lời mời liên kết", description = "Từ chối liên kết tài khoản từ phụ huynh kèm lý do")
    @PostMapping("/links/reject")
    public ResponseEntity<ApiResponse<ParentLinkResult>> rejectInvite(@Valid @RequestBody RespondLinkRequest request) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        ParentLinkResult result = manageParentLinkUseCase.rejectInvite(
                new RejectLinkCommand(studentId, request.inviteToken(), request.reason())
        );
        return ResponseEntity.ok(ApiResponse.success("Từ chối liên kết thành công", result));
    }

    @Operation(summary = "Xem danh sách phụ huynh đang liên kết", description = "Danh sách tài khoản phụ huynh có quyền theo dõi tiến độ học tập của học sinh")
    @GetMapping("/links")
    public ResponseEntity<ApiResponse<List<ParentLinkResult>>> getMyParentLinks() {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        List<ParentLinkResult> results = manageParentLinkUseCase.getStudentLinks(studentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phụ huynh liên kết thành công", results));
    }

    @Operation(summary = "Học sinh chủ động hủy liên kết với phụ huynh", description = "Thu hồi quyền xem dữ liệu học tập và chấm dứt mối liên kết")
    @PostMapping("/links/{linkId}/unlink")
    public ResponseEntity<ApiResponse<ParentLinkResult>> unlinkParent(
            @PathVariable UUID linkId,
            @RequestBody(required = false) UnlinkRequest request
    ) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        String reason = request != null ? request.reason() : "Học sinh chủ động hủy liên kết";
        ParentLinkResult result = manageParentLinkUseCase.unlink(new UnlinkCommand(studentId, linkId, reason));
        return ResponseEntity.ok(ApiResponse.success("Hủy liên kết thành công", result));
    }
}

