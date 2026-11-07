package com.beeacademy.backend.api.admin;

import com.beeacademy.backend.api.admin.dto.*;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.admin.dto.AdminUserDetailDto;
import com.beeacademy.backend.application.admin.dto.AdminUserSummaryDto;
import com.beeacademy.backend.application.admin.dto.ProvisionTeacherCommand;
import com.beeacademy.backend.application.admin.usecase.ManageUsersUseCase;
import com.beeacademy.backend.application.admin.usecase.ProvisionTeacherAccountUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin - User Management", description = "REST API quản trị người dùng, khóa/mở khóa tài khoản, cấp tài khoản giáo viên (UC39, UC40)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final ManageUsersUseCase manageUsersUseCase;
    private final ProvisionTeacherAccountUseCase provisionTeacherAccountUseCase;

    public AdminUserController(
            ManageUsersUseCase manageUsersUseCase,
            ProvisionTeacherAccountUseCase provisionTeacherAccountUseCase
    ) {
        this.manageUsersUseCase = manageUsersUseCase;
        this.provisionTeacherAccountUseCase = provisionTeacherAccountUseCase;
    }

    @GetMapping
    @Operation(summary = "Tìm kiếm và phân trang danh sách người dùng (UC39)")
    public ApiResponse<Page<AdminUserSummaryDto>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AdminUserSummaryDto> users = manageUsersUseCase.searchUsers(keyword, role, status, pageable);
        return ApiResponse.success("Lấy danh sách người dùng thành công", users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem thông tin chi tiết một người dùng")
    public ApiResponse<AdminUserDetailDto> getUserDetail(@PathVariable UUID id) {
        AdminUserDetailDto user = manageUsersUseCase.getUserDetail(id);
        return ApiResponse.success("Lấy thông tin người dùng thành công", user);
    }

    @PostMapping("/{id}/lock")
    @Operation(summary = "Khóa tài khoản người dùng (UC40)")
    public ApiResponse<Void> lockUser(
            @PathVariable UUID id,
            @Valid @RequestBody LockUserRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        manageUsersUseCase.lockUser(id, request.reason(), adminId);
        return ApiResponse.ok("Khóa tài khoản thành công");
    }

    @PostMapping("/{id}/unlock")
    @Operation(summary = "Mở khóa tài khoản người dùng (UC40)")
    public ApiResponse<Void> unlockUser(
            @PathVariable UUID id,
            @RequestBody(required = false) UnlockUserRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        String reason = request != null ? request.reason() : null;
        manageUsersUseCase.unlockUser(id, reason, adminId);
        return ApiResponse.ok("Mở khóa tài khoản thành công");
    }

    @PostMapping("/{id}/approve-teacher")
    @Operation(summary = "Phê duyệt hồ sơ đăng ký giảng dạy của giáo viên")
    public ApiResponse<Void> approveTeacher(@PathVariable UUID id) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        provisionTeacherAccountUseCase.approveTeacher(id, adminId);
        return ApiResponse.ok("Phê duyệt giáo viên thành công");
    }

    @PostMapping("/{id}/reject-teacher")
    @Operation(summary = "Từ chối hồ sơ đăng ký giảng dạy của giáo viên")
    public ApiResponse<Void> rejectTeacher(
            @PathVariable UUID id,
            @Valid @RequestBody RejectTeacherRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        provisionTeacherAccountUseCase.rejectTeacher(id, request.reason(), adminId);
        return ApiResponse.ok("Từ chối hồ sơ giáo viên thành công");
    }

    @PostMapping("/provision-teacher")
    @Operation(summary = "Cấp mới tài khoản giáo viên trực tiếp từ Ban Quản trị")
    public ApiResponse<AdminUserDetailDto> provisionTeacher(
            @Valid @RequestBody ProvisionTeacherRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        ProvisionTeacherCommand command = new ProvisionTeacherCommand(
                request.email(),
                request.temporaryPassword(),
                request.fullName(),
                request.contactNote()
        );
        AdminUserDetailDto teacher = provisionTeacherAccountUseCase.provisionTeacherAccount(command, adminId);
        return ApiResponse.success("Cấp mới tài khoản giáo viên thành công", teacher);
    }
}

