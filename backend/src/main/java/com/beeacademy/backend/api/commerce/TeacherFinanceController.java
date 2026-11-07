package com.beeacademy.backend.api.commerce;

import com.beeacademy.backend.api.commerce.dto.TeacherBankRequest;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.commerce.dto.PayoutPeriodResult;
import com.beeacademy.backend.application.commerce.dto.TeacherBankAccountCommand;
import com.beeacademy.backend.application.commerce.dto.TeacherBankAccountResult;
import com.beeacademy.backend.application.commerce.dto.TeacherRevenueStatsResult;
import com.beeacademy.backend.application.commerce.usecase.ManagePayoutUseCase;
import com.beeacademy.backend.application.commerce.usecase.ManageTeacherBankUseCase;
import com.beeacademy.backend.platform.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "16. Tài chính & Quyết toán Giáo viên (Teacher Finance & Payouts)", description = "Các API cấu hình tài khoản ngân hàng (mã hóa AES-256-GCM), xem thống kê doanh thu 80% và lịch sử các kỳ đối soát")
@RestController
@RequestMapping("/api/teacher/finance")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherFinanceController {

    private final ManageTeacherBankUseCase manageTeacherBankUseCase;
    private final ManagePayoutUseCase managePayoutUseCase;

    public TeacherFinanceController(
            ManageTeacherBankUseCase manageTeacherBankUseCase,
            ManagePayoutUseCase managePayoutUseCase
    ) {
        this.manageTeacherBankUseCase = manageTeacherBankUseCase;
        this.managePayoutUseCase = managePayoutUseCase;
    }

    @Operation(summary = "Lấy thông tin tài khoản ngân hàng của giáo viên", description = "Trả về thông tin số tài khoản dạng mặt nạ (ví dụ ******6789) và trạng thái thẩm định")
    @GetMapping("/bank-account")
    public ResponseEntity<ApiResponse<TeacherBankAccountResult>> getMyBankAccount() {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        return manageTeacherBankUseCase.getBankAccount(teacherId)
                .map(res -> ResponseEntity.ok(ApiResponse.success("Lấy thông tin tài khoản ngân hàng thành công", res)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.success("Chưa thiết lập tài khoản ngân hàng", null)));
    }

    @Operation(summary = "Cập nhật tài khoản ngân hàng nhận tiền", description = "Lưu số tài khoản mới (được mã hóa AES-256-GCM bảo mật) và chuyển trạng thái về PENDING chờ thẩm định")
    @PostMapping("/bank-account")
    public ResponseEntity<ApiResponse<TeacherBankAccountResult>> updateBankAccount(@Valid @RequestBody TeacherBankRequest request) {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        TeacherBankAccountCommand command = new TeacherBankAccountCommand(
                request.bankName(),
                request.accountNumber(),
                request.accountHolder(),
                request.branch()
        );
        TeacherBankAccountResult result = manageTeacherBankUseCase.saveOrUpdateBankAccount(teacherId, command);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tài khoản ngân hàng thành công", result));
    }

    @Operation(summary = "Lấy lịch sử các kỳ quyết toán của giáo viên", description = "Danh sách các kỳ quyết toán theo tháng (YYYY-MM), tổng doanh thu, phí sàn 20%, phần thực nhận 80% và trạng thái chi trả")
    @GetMapping("/payouts")
    public ResponseEntity<ApiResponse<List<PayoutPeriodResult>>> getMyPayouts() {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        List<PayoutPeriodResult> results = managePayoutUseCase.getTeacherPayouts(teacherId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách kỳ quyết toán thành công", results));
    }

    @Operation(summary = "Xem tổng quan thống kê tài chính & doanh thu", description = "Tổng doanh thu tích lũy, số tiền đang chờ chi trả và số tiền đã được thanh toán")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<TeacherRevenueStatsResult>> getFinanceStatistics() {
        UUID teacherId = SecurityUtils.requireCurrentUserId();
        TeacherRevenueStatsResult stats = managePayoutUseCase.getTeacherRevenueStatistics(teacherId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê tài chính thành công", stats));
    }
}

