package com.beeacademy.backend.api.auth;

import com.beeacademy.backend.api.auth.dto.ChangePasswordRequest;
import com.beeacademy.backend.api.auth.dto.UpdateProfileRequest;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.auth.dto.ChangePasswordCommand;
import com.beeacademy.backend.application.auth.dto.UpdateProfileCommand;
import com.beeacademy.backend.application.auth.dto.UserProfileResult;
import com.beeacademy.backend.application.auth.usecase.ChangePasswordUseCase;
import com.beeacademy.backend.application.auth.usecase.ManageProfileUseCase;
import com.beeacademy.backend.application.media.UploadMediaUseCase;
import com.beeacademy.backend.domain.media.MediaUploadResult;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Controller quản lý Hồ sơ người dùng và Đổi mật khẩu (UC04, UC05):
 * - Xem hồ sơ bản thân (/me)
 * - Cập nhật thông tin cá nhân
 * - Đổi mật khẩu
 * - Tải lên / Cập nhật ảnh đại diện
 */
@Tag(name = "2. Hồ sơ cá nhân (Profile)", description = "Các API xem thông tin cá nhân, cập nhật hồ sơ và đổi mật khẩu chủ động")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ManageProfileUseCase manageProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UploadMediaUseCase uploadMediaUseCase;

    public ProfileController(ManageProfileUseCase manageProfileUseCase,
                             ChangePasswordUseCase changePasswordUseCase,
                             UploadMediaUseCase uploadMediaUseCase) {
        this.manageProfileUseCase = manageProfileUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.uploadMediaUseCase = uploadMediaUseCase;
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
     * Tải lên file ảnh đại diện (multipart/form-data).
     */
    @Operation(summary = "Tải lên ảnh đại diện từ file", description = "Tải ảnh từ máy tính (JPG, PNG, WEBP tối đa 5MB) để đặt làm ảnh đại diện")
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResult>> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        UUID currentUserId = SecurityUtils.requireCurrentUserId();
        MediaUploadResult uploadResult = uploadMediaUseCase.uploadAvatar(file.getBytes(), file.getOriginalFilename(), currentUserId);
        UserProfileResult updated = manageProfileUseCase.updateAvatar(currentUserId, uploadResult.url());
        return ResponseEntity.ok(ApiResponse.success("Tải lên ảnh đại diện thành công", updated));
    }

    /**
     * Cập nhật avatar ảnh đại diện bằng URL (cho Google OAuth hoặc liên kết ngoài).
     */
    @PutMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserProfileResult>> updateAvatar(@RequestBody AvatarUpdateRequest request) {
        UUID currentUserId = SecurityUtils.requireCurrentUserId();
        UserProfileResult updated = manageProfileUseCase.updateAvatar(currentUserId, request.avatarUrl());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ảnh đại diện thành công", updated));
    }

    /**
     * Xóa ảnh đại diện (quay về mặc định).
     */
    @Operation(summary = "Xóa ảnh đại diện", description = "Xóa ảnh đại diện hiện tại để quay về avatar mặc định theo tên")
    @DeleteMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserProfileResult>> removeAvatar() {
        UUID currentUserId = SecurityUtils.requireCurrentUserId();
        UserProfileResult updated = manageProfileUseCase.updateAvatar(currentUserId, null);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa ảnh đại diện", updated));
    }

    public record AvatarUpdateRequest(String avatarUrl) {}
}

