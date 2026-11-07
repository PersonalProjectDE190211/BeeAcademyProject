package com.beeacademy.backend.api.support;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.api.support.dto.AddComplaintMessageRequest;
import com.beeacademy.backend.api.support.dto.ResolveComplaintRequest;
import com.beeacademy.backend.application.support.dto.AddComplaintMessageCommand;
import com.beeacademy.backend.application.support.dto.ComplaintResult;
import com.beeacademy.backend.application.support.dto.ResolveComplaintCommand;
import com.beeacademy.backend.application.support.usecase.ManageComplaintUseCase;
import com.beeacademy.backend.domain.support.ComplaintStatus;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "21. Quản trị Khiếu nại (Admin Complaints Management)", description = "Các API Admin kiểm duyệt, trả lời hỗ trợ và đánh dấu giải quyết khiếu nại")
@RestController
@RequestMapping("/api/admin/complaints")
@PreAuthorize("hasRole('ADMIN')")
public class AdminComplaintController {

    private final ManageComplaintUseCase manageComplaintUseCase;

    public AdminComplaintController(ManageComplaintUseCase manageComplaintUseCase) {
        this.manageComplaintUseCase = manageComplaintUseCase;
    }

    @Operation(summary = "Xem toàn bộ khiếu nại trong hệ thống", description = "Admin lọc khiếu nại theo trạng thái (OPEN, IN_PROGRESS, RESOLVED, CLOSED)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ComplaintResult>>> getAllComplaints(
            @RequestParam(required = false) String status
    ) {
        ComplaintStatus complaintStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                complaintStatus = ComplaintStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        List<ComplaintResult> results = manageComplaintUseCase.getAllComplaints(complaintStatus);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khiếu nại thành công", results));
    }

    @Operation(summary = "Admin giải quyết khiếu nại", description = "Cập nhật trạng thái RESOLVED và đính kèm tóm tắt kết quả xử lý cho người dùng")
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<ComplaintResult>> resolveComplaint(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveComplaintRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        ResolveComplaintCommand command = new ResolveComplaintCommand(id, adminId, request.resolutionSummary());
        ComplaintResult result = manageComplaintUseCase.resolveComplaint(command);
        return ResponseEntity.ok(ApiResponse.success("Giải quyết khiếu nại thành công", result));
    }

    @Operation(summary = "Admin gửi tin nhắn trả lời trong khiếu nại", description = "Admin phản hồi và tự động chuyển trạng thái khiếu nại sang IN_PROGRESS nếu đang OPEN")
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<ComplaintResult>> addAdminMessage(
            @PathVariable UUID id,
            @Valid @RequestBody AddComplaintMessageRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        AddComplaintMessageCommand command = new AddComplaintMessageCommand(id, adminId, "ADMIN", request.content());
        ComplaintResult result = manageComplaintUseCase.addMessage(command);
        return ResponseEntity.ok(ApiResponse.success("Gửi phản hồi thành công", result));
    }
}

