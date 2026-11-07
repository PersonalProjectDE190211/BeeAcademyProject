package com.beeacademy.backend.api.admin;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.admin.dto.AdminDashboardDto;
import com.beeacademy.backend.application.admin.usecase.GetAdminDashboardUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin - Dashboard", description = "REST API báo cáo tổng quan và số liệu thống kê hệ thống (UC38)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final GetAdminDashboardUseCase getAdminDashboardUseCase;

    public AdminDashboardController(GetAdminDashboardUseCase getAdminDashboardUseCase) {
        this.getAdminDashboardUseCase = getAdminDashboardUseCase;
    }

    @GetMapping("/summary")
    @Operation(summary = "Xem số liệu thống kê tổng quan (GMV, người dùng, khóa học, hỗ trợ)")
    public ApiResponse<AdminDashboardDto> getDashboardSummary() {
        AdminDashboardDto summary = getAdminDashboardUseCase.execute();
        return ApiResponse.success("Lấy số liệu dashboard quản trị thành công", summary);
    }
}

