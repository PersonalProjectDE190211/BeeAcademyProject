package com.beeacademy.backend.api.commerce;

import com.beeacademy.backend.application.commerce.dto.PayOsWebhookPayload;
import com.beeacademy.backend.application.commerce.dto.WebhookProcessResult;
import com.beeacademy.backend.application.commerce.usecase.ProcessPaymentWebhookUseCase;
import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PaymentWebhookController MockMvc Tests")
class PaymentWebhookControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;

    @BeforeEach
    void setUp() {
        processPaymentWebhookUseCase = Mockito.mock(ProcessPaymentWebhookUseCase.class);
        PaymentWebhookController controller = new PaymentWebhookController(processPaymentWebhookUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/webhooks/payos - Nhận webhook thành công")
    void handlePayOsWebhook_Success() throws Exception {
        PayOsWebhookPayload payload = new PayOsWebhookPayload(
                "00",
                "success",
                true,
                new PayOsWebhookPayload.PayOsWebhookData(8899L, 500000, "BEE8899", "123", "FT123", "2026-05-01", "VND", "PLINK", "00", "success"),
                "signature"
        );

        WebhookProcessResult result = new WebhookProcessResult(
                true, UUID.randomUUID(), 8899L, "PAID", "Xác nhận thanh toán thành công"
        );

        when(processPaymentWebhookUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(post("/api/webhooks/payos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderCode").value(8899))
                .andExpect(jsonPath("$.data.orderStatus").value("PAID"));
    }
}

