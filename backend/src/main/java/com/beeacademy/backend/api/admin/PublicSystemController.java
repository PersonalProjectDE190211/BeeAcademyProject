package com.beeacademy.backend.api.admin;

import com.beeacademy.backend.api.admin.dto.MaintenanceStatusResponse;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.admin.usecase.ManageSystemSettingsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/system")
@Tag(name = "Public - System Status", description = "REST API công khai kiểm tra trạng thái bảo trì hệ thống")
public class PublicSystemController {

    private final ManageSystemSettingsUseCase manageSystemSettingsUseCase;

    public PublicSystemController(ManageSystemSettingsUseCase manageSystemSettingsUseCase) {
        this.manageSystemSettingsUseCase = manageSystemSettingsUseCase;
    }

    @GetMapping("/maintenance")
    @Operation(summary = "Kiểm tra hệ thống có đang ở chế độ bảo trì hay không")
    public ApiResponse<MaintenanceStatusResponse> checkMaintenanceStatus() {
        ManageSystemSettingsUseCase.MaintenanceStatus status = manageSystemSettingsUseCase.getMaintenanceStatus();
        return ApiResponse.success(new MaintenanceStatusResponse(status.enabled(), status.message()));
    }
}
