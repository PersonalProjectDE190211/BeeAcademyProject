package com.beeacademy.backend.api.commerce;

import com.beeacademy.backend.api.commerce.dto.CreateOrderRequest;
import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.commerce.dto.CreateOrderCommand;
import com.beeacademy.backend.application.commerce.dto.CreateOrderResult;
import com.beeacademy.backend.application.commerce.dto.OrderResult;
import com.beeacademy.backend.application.commerce.usecase.CreateOrderUseCase;
import com.beeacademy.backend.application.commerce.usecase.ManageOrderUseCase;
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

@Tag(name = "14. Đơn hàng & Thanh toán Học viên (Student Orders & Checkout)", description = "Các API tạo đơn mua khóa học, lấy link thanh toán QR PayOS, xem lịch sử đơn hàng và hủy đơn")
@RestController
@RequestMapping("/api/student/orders")
@PreAuthorize("hasRole('STUDENT')")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final ManageOrderUseCase manageOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase, ManageOrderUseCase manageOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.manageOrderUseCase = manageOrderUseCase;
    }

    @Operation(summary = "Tạo đơn hàng mua khóa học", description = "Tạo đơn hàng mới (hết hạn sau 15 phút), sinh mã QR thanh toán PayOS và mã đơn hàng duy nhất")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResult>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        CreateOrderCommand command = new CreateOrderCommand(
                studentId,
                request.courseIds(),
                request.voucherCode(),
                request.paymentMethod()
        );
        CreateOrderResult result = createOrderUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Khởi tạo đơn hàng thành công", result));
    }

    @Operation(summary = "Lấy danh sách đơn hàng của học sinh", description = "Truy vấn toàn bộ lịch sử đơn hàng của học sinh hiện tại sắp xếp theo thời gian mới nhất")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResult>>> getMyOrders() {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        List<OrderResult> results = manageOrderUseCase.getStudentOrders(studentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", results));
    }

    @Operation(summary = "Xem chi tiết một đơn hàng", description = "Lấy thông tin chi tiết các khóa học trong đơn, tổng tiền, mã tham chiếu thanh toán và hạn thanh toán")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResult>> getOrderDetail(@PathVariable UUID id) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        OrderResult result = manageOrderUseCase.getOrderById(id, studentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin đơn hàng thành công", result));
    }

    @Operation(summary = "Chủ động đối chiếu & xác nhận thanh toán đơn hàng từ PayOS", description = "Kiểm tra trực tiếp trạng thái giao dịch trên cổng PayOS và tự động kích hoạt khóa học nếu đã thanh toán thành công")
    @PostMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<OrderResult>> verifyOrderPayment(@PathVariable UUID id) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        OrderResult result = manageOrderUseCase.verifyOrderPayment(id, studentId);
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra trạng thái thanh toán thành công", result));
    }

    @Operation(summary = "Hủy đơn hàng đang chờ thanh toán", description = "Học sinh chủ động hủy đơn hàng khi không còn nhu cầu thanh toán")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResult>> cancelOrder(@PathVariable UUID id) {
        UUID studentId = SecurityUtils.requireCurrentUserId();
        OrderResult result = manageOrderUseCase.cancelOrder(id, studentId);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công", result));
    }
}

