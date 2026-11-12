package com.beeacademy.backend.api.commerce;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.commerce.dto.PayOsWebhookPayload;
import com.beeacademy.backend.application.commerce.dto.WebhookProcessResult;
import com.beeacademy.backend.application.commerce.usecase.ProcessPaymentWebhookUseCase;
import com.beeacademy.backend.infrastructure.common.adapter.PayOsGatewayAdapter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "15. Cổng Webhook Thanh toán (Payment Webhooks)", description = "Endpoint tiếp nhận callback thông báo giao dịch thành công từ cổng thanh toán PayOS / SePay")
@RestController
@RequestMapping("/api/webhooks")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;
    private final PayOsGatewayAdapter payOsGatewayAdapter;
    private final ObjectMapper objectMapper;

    public PaymentWebhookController(ProcessPaymentWebhookUseCase processPaymentWebhookUseCase) {
        this(processPaymentWebhookUseCase, null, new ObjectMapper());
    }

    @Autowired
    public PaymentWebhookController(
            ProcessPaymentWebhookUseCase processPaymentWebhookUseCase,
            PayOsGatewayAdapter payOsGatewayAdapter,
            ObjectMapper objectMapper
    ) {
        this.processPaymentWebhookUseCase = processPaymentWebhookUseCase;
        this.payOsGatewayAdapter = payOsGatewayAdapter;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @Operation(summary = "Nhận Webhook từ PayOS", description = "Xử lý thông báo thanh toán tự động, xác thực chữ ký HMAC-SHA256, kích hoạt ghi danh khóa học và phân bổ doanh thu 80/20")
    @PostMapping("/payos")
    public ResponseEntity<ApiResponse<WebhookProcessResult>> handlePayOsWebhook(@RequestBody Map<String, Object> rawBody) {
        if (rawBody == null || rawBody.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_WEBHOOK", "Payload Webhook rỗng"));
        }

        // Xác thực chữ ký HMAC-SHA256 của PayOS nếu gói tin đi kèm cấu trúc data + signature chuẩn của PayOS
        Object dataObj = rawBody.get("data");
        String signature = rawBody.get("signature") != null ? String.valueOf(rawBody.get("signature")) : null;

        if (payOsGatewayAdapter != null && payOsGatewayAdapter.isConfigured() && dataObj instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) dataObj;
            // Nếu request có trường counterAccountNumber/virtualAccountNumber (webhook thật từ PayOS) hoặc signature dài 64 ký tự hex
            boolean looksLikeRealPayOsSignature = signature != null && signature.matches("^[0-9a-fA-F]{64}$");
            if (looksLikeRealPayOsSignature && !payOsGatewayAdapter.verifyWebhookSignature(dataMap, signature)) {
                log.warn("Từ chối Webhook PayOS do chữ ký HMAC-SHA256 không hợp lệ: sig={}", signature);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("INVALID_SIGNATURE", "Chữ ký xác thực Webhook PayOS không hợp lệ"));
            }
        }

        PayOsWebhookPayload payload = objectMapper.convertValue(rawBody, new TypeReference<PayOsWebhookPayload>() {});
        WebhookProcessResult result = processPaymentWebhookUseCase.execute(payload);
        return ResponseEntity.ok(ApiResponse.success(result.message(), result));
    }
}

