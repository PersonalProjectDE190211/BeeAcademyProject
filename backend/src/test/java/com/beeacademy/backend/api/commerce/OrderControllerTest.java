package com.beeacademy.backend.api.commerce;

import com.beeacademy.backend.api.commerce.dto.CreateOrderRequest;
import com.beeacademy.backend.application.commerce.dto.CreateOrderResult;
import com.beeacademy.backend.application.commerce.dto.OrderItemResult;
import com.beeacademy.backend.application.commerce.dto.OrderResult;
import com.beeacademy.backend.application.commerce.usecase.CreateOrderUseCase;
import com.beeacademy.backend.application.commerce.usecase.ManageOrderUseCase;
import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.platform.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("OrderController MockMvc Tests")
class OrderControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private CreateOrderUseCase createOrderUseCase;
    private ManageOrderUseCase manageOrderUseCase;

    private final UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        createOrderUseCase = Mockito.mock(CreateOrderUseCase.class);
        manageOrderUseCase = Mockito.mock(ManageOrderUseCase.class);

        OrderController controller = new OrderController(createOrderUseCase, manageOrderUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserPrincipal principal = new UserPrincipal(studentId, "student@bee.edu.vn", "ROLE_STUDENT");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/student/orders - Tạo đơn hàng thành công")
    void createOrder_Success() throws Exception {
        UUID courseId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest(List.of(courseId), null, "PAYOS");

        OrderItemResult item = new OrderItemResult(
                UUID.randomUUID(), courseId, UUID.randomUUID(), "Toán 10", 300000, 20, 60000, 240000, "PENDING"
        );
        CreateOrderResult result = new CreateOrderResult(
                UUID.randomUUID(), 123456L, "BEE123456", "https://pay.payos.vn/web/123",
                "https://img.vietqr.io/123", 300000, 0, "PENDING", Instant.now(), List.of(item)
        );

        when(createOrderUseCase.execute(any())).thenReturn(result);

        mockMvc.perform(post("/api/student/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderCode").value(123456))
                .andExpect(jsonPath("$.data.totalAmountVnd").value(300000))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/student/orders - Lấy danh sách đơn hàng")
    void getMyOrders_Success() throws Exception {
        OrderResult orderResult = new OrderResult(
                UUID.randomUUID(), studentId, 123456L, "BEE123456", "PAYOS", "PAID",
                300000, 0, Instant.now(), Instant.now(), Instant.now(), Instant.now(), Collections.emptyList()
        );

        when(manageOrderUseCase.getStudentOrders(eq(studentId))).thenReturn(List.of(orderResult));

        mockMvc.perform(get("/api/student/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].orderCode").value(123456))
                .andExpect(jsonPath("$.data[0].status").value("PAID"));
    }
}

