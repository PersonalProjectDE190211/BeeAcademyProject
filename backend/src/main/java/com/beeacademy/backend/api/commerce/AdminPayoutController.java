package com.beeacademy.backend.api.commerce;

import com.beeacademy.backend.api.commerce.dto.ConfirmPayoutRequest;
import com.beeacademy.backend.api.commerce.dto.RejectBankRequest;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.commerce.dto.ConfirmPayoutCommand;
import com.beeacademy.backend.application.commerce.dto.PayoutPeriodResult;
import com.beeacademy.backend.application.commerce.dto.TeacherBankAccountResult;
import com.beeacademy.backend.application.commerce.usecase.ManagePayoutUseCase;
import com.beeacademy.backend.application.commerce.usecase.ManageTeacherBankUseCase;
import com.beeacademy.backend.domain.commerce.PayoutStatus;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "17. Quản trị Tài chính & Quyết toán (Admin Finance & Payouts)", description = "Các API Admin kiểm duyệt tài khoản ngân hàng và xác nhận chi trả quyết toán định kỳ cho giáo viên")
@RestController
@RequestMapping("/api/admin/finance")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPayoutController {

    private final ManagePayoutUseCase managePayoutUseCase;
    private final ManageTeacherBankUseCase manageTeacherBankUseCase;

    public AdminPayoutController(
            ManagePayoutUseCase managePayoutUseCase,
            ManageTeacherBankUseCase manageTeacherBankUseCase
    ) {
        this.managePayoutUseCase = managePayoutUseCase;
        this.manageTeacherBankUseCase = manageTeacherBankUseCase;
    }

    @Operation(summary = "Xem danh sách các kỳ quyết toán trong hệ thống", description = "Quản trị viên lọc danh sách kỳ quyết toán theo trạng thái (PENDING, PROCESSING, PAID, REJECTED)")
    @GetMapping("/payouts")
    public ResponseEntity<ApiResponse<List<PayoutPeriodResult>>> getPayouts(
            @RequestParam(required = false) String status
    ) {
        PayoutStatus payoutStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                payoutStatus = PayoutStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        List<PayoutPeriodResult> results = managePayoutUseCase.getAllPayouts(payoutStatus);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách kỳ quyết toán thành công", results));
    }

    @Operation(summary = "Xác nhận chuyển khoản quyết toán cho giáo viên", description = "Admin nhập mã giao dịch ngân hàng (transferRef) và ảnh ủy nhiệm chi (UNC) để hoàn tất chi trả")
    @PostMapping("/payouts/{id}/confirm")
    public ResponseEntity<ApiResponse<PayoutPeriodResult>> confirmPayout(
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmPayoutRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        ConfirmPayoutCommand command = new ConfirmPayoutCommand(
                adminId,
                id,
                request.transferRef(),
                request.transferContent(),
                request.uncAttachmentUrl()
        );
        PayoutPeriodResult result = managePayoutUseCase.confirmPayout(command);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận chi trả quyết toán thành công", result));
    }

    @Operation(summary = "Xem danh sách tài khoản ngân hàng chờ thẩm định", description = "Danh sách tài khoản ngân hàng của giáo viên đang ở trạng thái PENDING")
    @GetMapping("/bank-accounts/pending")
    public ResponseEntity<ApiResponse<List<TeacherBankAccountResult>>> getPendingBankAccounts() {
        List<TeacherBankAccountResult> results = manageTeacherBankUseCase.getPendingBankAccounts();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài khoản chờ thẩm định thành công", results));
    }

    @Operation(summary = "Phê duyệt tài khoản ngân hàng của giáo viên", description = "Admin xác nhận thông tin tài khoản hợp lệ để sẵn sàng nhận tiền quyết toán")
    @PostMapping("/bank-accounts/{id}/verify")
    public ResponseEntity<ApiResponse<TeacherBankAccountResult>> verifyBankAccount(@PathVariable UUID id) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        TeacherBankAccountResult result = manageTeacherBankUseCase.verifyBankAccount(id, adminId);
        return ResponseEntity.ok(ApiResponse.success("Phê duyệt tài khoản ngân hàng thành công", result));
    }

    @Operation(summary = "Từ chối tài khoản ngân hàng của giáo viên", description = "Admin từ chối thông tin tài khoản kèm theo lý do cụ thể")
    @PostMapping("/bank-accounts/{id}/reject")
    public ResponseEntity<ApiResponse<TeacherBankAccountResult>> rejectBankAccount(
            @PathVariable UUID id,
            @Valid @RequestBody RejectBankRequest request
    ) {
        UUID adminId = SecurityUtils.requireCurrentUserId();
        TeacherBankAccountResult result = manageTeacherBankUseCase.rejectBankAccount(id, adminId, request.reason());
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối tài khoản ngân hàng", result));
    }
}

