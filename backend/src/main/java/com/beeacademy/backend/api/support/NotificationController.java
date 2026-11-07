package com.beeacademy.backend.api.support;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.support.dto.NotificationResult;
import com.beeacademy.backend.application.support.usecase.ManageNotificationUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "22. Thông báo Người dùng (User Notifications)", description = "Các API nhận thông báo hệ thống, đếm thông báo chưa đọc và đánh dấu đã đọc")
@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final ManageNotificationUseCase manageNotificationUseCase;

    public NotificationController(ManageNotificationUseCase manageNotificationUseCase) {
        this.manageNotificationUseCase = manageNotificationUseCase;
    }

    @Operation(summary = "Lấy tất cả thông báo của người dùng", description = "Danh sách thông báo mới nhất gửi đến tài khoản đang đăng nhập")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResult>>> getMyNotifications() {
        UUID recipientId = SecurityUtils.requireCurrentUserId();
        List<NotificationResult> results = manageNotificationUseCase.getUserNotifications(recipientId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo thành công", results));
    }

    @Operation(summary = "Lấy danh sách thông báo chưa đọc", description = "Truy vấn các thông báo chưa đọc để hiển thị trên chuông thông báo (Bell Icon)")
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResult>>> getUnreadNotifications() {
        UUID recipientId = SecurityUtils.requireCurrentUserId();
        List<NotificationResult> results = manageNotificationUseCase.getUnreadUserNotifications(recipientId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thông báo chưa đọc thành công", results));
    }

    @Operation(summary = "Đánh dấu thông báo đã đọc", description = "Cập nhật trạng thái thông báo thành đã đọc và ghi nhận thời điểm readAt")
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResult>> markAsRead(@PathVariable UUID id) {
        UUID recipientId = SecurityUtils.requireCurrentUserId();
        NotificationResult result = manageNotificationUseCase.markAsRead(id, recipientId);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu đã đọc thành công", result));
    }
}

