package com.beeacademy.backend.api.admin;

import com.beeacademy.backend.api.admin.dto.MaintenanceModeRequest;
import com.beeacademy.backend.api.admin.dto.UpdateSettingRequest;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.admin.dto.AdminAuditLogDto;
import com.beeacademy.backend.application.admin.usecase.ManageSystemSettingsUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/settings")
@Tag(name = "Admin - System Settings", description = "REST API quản trị cấu hình hệ thống, chế độ bảo trì và nhật ký kiểm toán")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemSettingsController {

    private final ManageSystemSettingsUseCase manageSystemSettingsUseCase;

    public AdminSystemSettingsController(ManageSystemSettingsUseCase manageSystemSettingsUseCase) {
        this.manageSystemSettingsUseCase = manageSystemSettingsUseCase;
    }

    @GetMapping
    @Operation(summary = "Xem toàn bộ cấu hình hệ thống hiện tại")
    public ApiResponse<Map<String, String>> getAllSettings() {
        Map<String, String> settings = manageSystemSettingsUseCase.getAllSettings();
        return ApiResponse.success("Lấy danh sách cấu hình hệ thống thành công", settings);
    }

    @PostMapping("/maintenance")
    @Operation(summary = "Bật hoặc tắt chế độ bảo trì toàn hệ thống")
    public ApiResponse<Void> toggleMaintenanceMode(@Valid @RequestBody MaintenanceModeRequest request) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        manageSystemSettingsUseCase.toggleMaintenanceMode(request.enabled(), request.message(), adminId);
        String msg = request.enabled() ? "Đã kích hoạt chế độ bảo trì" : "Đã hủy kích hoạt chế độ bảo trì";
        return ApiResponse.ok(msg);
    }

    @PutMapping("/{key}")
    @Operation(summary = "Cập nhật giá trị cấu hình hệ thống theo khóa")
    public ApiResponse<Void> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody UpdateSettingRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        manageSystemSettingsUseCase.updateSetting(key, request.value(), request.reason(), adminId);
        return ApiResponse.ok("Cập nhật cấu hình thành công");
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Xem danh sách nhật ký kiểm toán tập trung (Audit Logs) phân trang")
    public ApiResponse<Page<AdminAuditLogDto>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AdminAuditLogDto> logs = manageSystemSettingsUseCase.getAuditLogs(entityType, pageable);
        return ApiResponse.success("Lấy danh sách nhật ký kiểm toán thành công", logs);
    }
}

