package com.beeacademy.backend.api.commerce;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.commerce.dto.PayOsWebhookPayload;
import com.beeacademy.backend.application.commerce.dto.WebhookProcessResult;
import com.beeacademy.backend.application.commerce.usecase.ProcessPaymentWebhookUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "15. Cổng Webhook Thanh toán (Payment Webhooks)", description = "Endpoint tiếp nhận callback thông báo giao dịch thành công từ cổng thanh toán PayOS / SePay")
@RestController
@RequestMapping("/api/webhooks")
public class PaymentWebhookController {

    private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;

    public PaymentWebhookController(ProcessPaymentWebhookUseCase processPaymentWebhookUseCase) {
        this.processPaymentWebhookUseCase = processPaymentWebhookUseCase;
    }

    @Operation(summary = "Nhận Webhook từ PayOS", description = "Xử lý thông báo thanh toán tự động, kích hoạt ghi danh khóa học và phân bổ doanh thu 80/20")
    @PostMapping("/payos")
    public ResponseEntity<ApiResponse<WebhookProcessResult>> handlePayOsWebhook(@RequestBody PayOsWebhookPayload payload) {
        WebhookProcessResult result = processPaymentWebhookUseCase.execute(payload);
        return ResponseEntity.ok(ApiResponse.success(result.message(), result));
    }
}

