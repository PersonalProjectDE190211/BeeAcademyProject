package com.beeacademy.backend.api.auth;

import com.beeacademy.backend.api.auth.dto.ChangePasswordRequest;
import com.beeacademy.backend.api.auth.dto.UpdateProfileRequest;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.auth.dto.ChangePasswordCommand;
import com.beeacademy.backend.application.auth.dto.UpdateProfileCommand;
import com.beeacademy.backend.application.auth.dto.UserProfileResult;
import com.beeacademy.backend.application.auth.usecase.ChangePasswordUseCase;
import com.beeacademy.backend.application.auth.usecase.ManageProfileUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller quản lý Hồ sơ người dùng và Đổi mật khẩu (UC04, UC05):
 * - Xem hồ sơ bản thân (/me)
 * - Cập nhật thông tin cá nhân
 * - Đổi mật khẩu
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ManageProfileUseCase manageProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    public ProfileController(ManageProfileUseCase manageProfileUseCase,
                             ChangePasswordUseCase changePasswordUseCase) {
        this.manageProfileUseCase = manageProfileUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
    }

    /**
     * UC04: Lấy thông tin hồ sơ của tài khoản đang đăng nhập.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResult>> getMyProfile() {
        UUID currentUserId = SecurityUtils.requireCurrentUserId();
        UserProfileResult profile = manageProfileUseCase.getProfile(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * UC04: Cập nhật thông tin cá nhân (họ tên, sđt, bio).
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResult>> updateMyProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UUID currentUserId = SecurityUtils.requireCurrentUserId();
        UpdateProfileCommand command = new UpdateProfileCommand(
                request.fullName(),
                request.phone(),
                request.bio()
        );
        UserProfileResult updated = manageProfileUseCase.updateProfile(currentUserId, command);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công", updated));
    }

    /**
     * UC05: Đổi mật khẩu chủ động (yêu cầu mật khẩu cũ).
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        UUID currentUserId = SecurityUtils.requireCurrentUserId();
        ChangePasswordCommand command = new ChangePasswordCommand(
                request.oldPassword(),
                request.newPassword()
        );
        changePasswordUseCase.execute(currentUserId, command);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công. Các phiên đăng nhập khác đã được thu hồi.", null));
    }

    /**
     * Cập nhật avatar ảnh đại diện.
     */
    @PutMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserProfileResult>> updateAvatar(@RequestBody AvatarUpdateRequest request) {
        UUID currentUserId = SecurityUtils.requireCurrentUserId();
        UserProfileResult updated = manageProfileUseCase.updateAvatar(currentUserId, request.avatarUrl());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh đại diện thành công", updated));
    }

    public record AvatarUpdateRequest(String avatarUrl) {}
}

