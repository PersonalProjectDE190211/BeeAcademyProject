package com.beeacademy.backend.api.ai;

import com.beeacademy.backend.api.ai.dto.ChatbotCourseCardDto;
import com.beeacademy.backend.api.ai.dto.ChatbotMessageResponse;
import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.ai.service.ChatbotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("ChatbotController MockMvc Tests")
class ChatbotControllerTest {

    private MockMvc mockMvc;
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = Mockito.mock(ChatbotService.class);
        ChatbotController controller = new ChatbotController(chatbotService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/chatbot/message thành công trả về cấu trúc JSON đúng chuẩn")
    void sendMessage_Success() throws Exception {
        UUID convId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        ChatbotCourseCardDto card = new ChatbotCourseCardDto(
                courseId,
                "Toán 6 — Khởi Đầu Vững Chắc: Số Học & Hình Học Trực Quan",
                "MATH",
                "Toán học",
                6,
                0,
                null,
                5.0,
                35,
                6,
                "https://img.com/math6.jpg",
                "/courses/" + courseId
        );

        ChatbotMessageResponse response = new ChatbotMessageResponse(
                convId,
                "BeeAcademy hiện có khóa học Toán 6 miễn phí.",
                "SEARCH_COURSE",
                List.of(card),
                List.of("Cách đăng ký học thử")
        );

        when(chatbotService.processMessage(any(), any(), eq("Tìm Toán lớp 6"))).thenReturn(response);

        mockMvc.perform(post("/api/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "conversationId": "%s",
                                  "message": "Tìm Toán lớp 6"
                                }
                                """.formatted(convId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.conversationId").value(convId.toString()))
                .andExpect(jsonPath("$.data.reply").value("BeeAcademy hiện có khóa học Toán 6 miễn phí."))
                .andExpect(jsonPath("$.data.intent").value("SEARCH_COURSE"))
                .andExpect(jsonPath("$.data.courses[0].id").value(courseId.toString()))
                .andExpect(jsonPath("$.data.courses[0].price").value(0))
                .andExpect(jsonPath("$.data.suggestions[0]").value("Cách đăng ký học thử"));
    }

    @Test
    @DisplayName("POST /api/chatbot/message với nội dung rỗng trả về HTTP 400 Bad Request")
    void sendMessage_EmptyMessage_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}

