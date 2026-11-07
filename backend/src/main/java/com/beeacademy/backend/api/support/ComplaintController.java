package com.beeacademy.backend.api.support;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.api.support.dto.AddComplaintMessageRequest;
import com.beeacademy.backend.api.support.dto.CreateComplaintRequest;
import com.beeacademy.backend.application.support.dto.AddComplaintMessageCommand;
import com.beeacademy.backend.application.support.dto.ComplaintResult;
import com.beeacademy.backend.application.support.dto.CreateComplaintCommand;
import com.beeacademy.backend.application.support.usecase.ManageComplaintUseCase;
import com.beeacademy.backend.domain.support.ComplaintCategory;
import com.beeacademy.backend.domain.support.ComplaintPriority;
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

@Tag(name = "20. Khiếu nại & Hỗ trợ người dùng (Complaints & Support Tickets)", description = "Các API tạo phiếu hỗ trợ, gửi phản hồi trao đổi 2 chiều và theo dõi tiến độ xử lý")
@RestController
@RequestMapping("/api/complaints")
@PreAuthorize("isAuthenticated()")
public class ComplaintController {

    private final ManageComplaintUseCase manageComplaintUseCase;

    public ComplaintController(ManageComplaintUseCase manageComplaintUseCase) {
        this.manageComplaintUseCase = manageComplaintUseCase;
    }

    @Operation(summary = "Tạo phiếu khiếu nại / yêu cầu hỗ trợ mới", description = "Người dùng (Học sinh, Phụ huynh, Giáo viên) gửi khiếu nại kèm phân loại và mức độ ưu tiên")
    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintResult>> createComplaint(@Valid @RequestBody CreateComplaintRequest request) {
        UUID senderId = SecurityUtils.requireCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole().orElse("STUDENT").replace("ROLE_", "");

        ComplaintCategory category = ComplaintCategory.OTHER;
        if (request.category() != null) {
            try {
                category = ComplaintCategory.valueOf(request.category().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        ComplaintPriority priority = ComplaintPriority.MEDIUM;
        if (request.priority() != null) {
            try {
                priority = ComplaintPriority.valueOf(request.priority().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        CreateComplaintCommand command = new CreateComplaintCommand(
                senderId,
                role,
                request.title(),
                category,
                priority,
                request.content()
        );

        ComplaintResult result = manageComplaintUseCase.createComplaint(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phiếu khiếu nại thành công", result));
    }

    @Operation(summary = "Xem danh sách khiếu nại của tôi", description = "Lấy tất cả các phiếu khiếu nại do tài khoản hiện tại khởi tạo")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ComplaintResult>>> getMyComplaints() {
        UUID senderId = SecurityUtils.requireCurrentUserId();
        List<ComplaintResult> results = manageComplaintUseCase.getMyComplaints(senderId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khiếu nại thành công", results));
    }

    @Operation(summary = "Xem chi tiết một phiếu khiếu nại", description = "Xem thông tin trạng thái, mức độ ưu tiên và toàn bộ luồng tin nhắn trao đổi")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResult>> getComplaintById(@PathVariable UUID id) {
        UUID requesterId = SecurityUtils.requireCurrentUserId();
        boolean isAdmin = SecurityUtils.getCurrentUserRole().map(r -> r.contains("ADMIN")).orElse(false);
        ComplaintResult result = manageComplaintUseCase.getComplaintById(id, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khiếu nại thành công", result));
    }

    @Operation(summary = "Gửi thêm tin nhắn phản hồi vào phiếu khiếu nại", description = "Người dùng hoặc quản trị viên gửi tin nhắn trao đổi trong luồng khiếu nại")
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<ComplaintResult>> addMessage(
            @PathVariable UUID id,
            @Valid @RequestBody AddComplaintMessageRequest request
    ) {
        UUID authorId = SecurityUtils.requireCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole().orElse("STUDENT").replace("ROLE_", "");

        AddComplaintMessageCommand command = new AddComplaintMessageCommand(
                id, authorId, role, request.content()
        );

        ComplaintResult result = manageComplaintUseCase.addMessage(command);
        return ResponseEntity.ok(ApiResponse.success("Gửi phản hồi thành công", result));
    }

    @Operation(summary = "Đóng phiếu khiếu nại", description = "Người dùng chủ động kết thúc phiếu hỗ trợ khi đã giải quyết xong vấn đề")
    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<ComplaintResult>> closeComplaint(@PathVariable UUID id) {
        UUID requesterId = SecurityUtils.requireCurrentUserId();
        boolean isAdmin = SecurityUtils.getCurrentUserRole().map(r -> r.contains("ADMIN")).orElse(false);
        ComplaintResult result = manageComplaintUseCase.closeComplaint(id, requesterId, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Đóng phiếu khiếu nại thành công", result));
    }
}

