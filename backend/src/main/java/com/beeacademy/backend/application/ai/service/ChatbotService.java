package com.beeacademy.backend.application.ai.service;

import com.beeacademy.backend.api.ai.dto.AiChatRequest;
import com.beeacademy.backend.api.ai.dto.ChatbotCourseCardDto;
import com.beeacademy.backend.api.ai.dto.ChatbotMessageResponse;
import com.beeacademy.backend.domain.ai.*;
import com.beeacademy.backend.infrastructure.common.adapter.GeminiAiAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service điều phối toàn bộ quá trình tư vấn khóa học BeeBot qua 4 giai đoạn.
 */
@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private final GeminiAiAdapter geminiAiAdapter;
    private final CourseSearchService courseSearchService;
    private final ConversationService conversationService;
    private final ObjectMapper objectMapper;

    public ChatbotService(GeminiAiAdapter geminiAiAdapter,
                          CourseSearchService courseSearchService,
                          ConversationService conversationService,
                          ObjectMapper objectMapper) {
        this.geminiAiAdapter = geminiAiAdapter;
        this.courseSearchService = courseSearchService;
        this.conversationService = conversationService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ChatbotMessageResponse processMessage(UUID conversationId, UUID userId, String userMessage) {
        // 1. Quản lý trạng thái hội thoại: lấy hoặc tạo mới ConversationContext
        ConversationContext context = conversationService.getOrCreateContext(conversationId, userId);
        UUID convId = context.conversationId();

        // 2. Lấy lịch sử hội thoại gần nhất
        List<AiChatRequest.ChatMessageDto> history = conversationService.getRecentHistory(convId, 6);

        // 3. GIAI ĐOẠN 1: Gemini NLU phân tích ý định & trích xuất điều kiện tìm kiếm
        IntentAnalysisResult nluResult = geminiAiAdapter.analyzeUserIntent(userMessage, context.currentFilters(), history);
        log.info("Giai đoạn 1 (NLU): intent={}, subject={}, grade={}, isCorrection={}",
                nluResult.intent(), nluResult.subject(), nluResult.grade(), nluResult.isCorrection());

        // Cập nhật trạng thái bộ lọc (State Merging)
        CourseSearchFilter mergedFilters = context.currentFilters().mergeWith(nluResult.toFilter());
        ChatbotIntent currentIntent = nluResult.intent();

        // 4. GIAI ĐOẠN 2 & 4: Backend tìm kiếm & Hậu kiểm khóa học từ Database
        List<ChatbotCourseCardDto> verifiedCourses = Collections.emptyList();
        boolean shouldSearchCourses = isSearchIntent(currentIntent) || !mergedFilters.isEmpty();
        if (shouldSearchCourses) {
            verifiedCourses = courseSearchService.searchAndVerify(mergedFilters);
        }

        // 5. GIAI ĐOẠN 3: Gemini tạo câu trả lời tư vấn dựa trên danh sách khóa học thực tế
        String courseKnowledge = courseSearchService.buildCourseKnowledgeSummary(verifiedCourses, mergedFilters);
        String reply = geminiAiAdapter.generateAdvisoryReply(
                userMessage,
                currentIntent,
                mergedFilters,
                verifiedCourses,
                courseKnowledge,
                history
        );

        // 6. Gợi ý câu hỏi tiếp theo thông minh
        List<String> suggestions = generateSmartSuggestions(currentIntent, mergedFilters, verifiedCourses);

        // 7. Cập nhật Context & Lưu lịch sử tin nhắn
        List<UUID> courseIds = verifiedCourses.stream().map(ChatbotCourseCardDto::id).toList();
        ConversationContext updatedContext = context.withUpdatedState(currentIntent, mergedFilters, courseIds);
        conversationService.saveContext(updatedContext);

        String coursesJson = "[]";
        try {
            coursesJson = objectMapper.writeValueAsString(verifiedCourses);
        } catch (Exception e) {
            log.warn("Lỗi tuần tự hóa verifiedCourses: {}", e.getMessage());
        }

        conversationService.recordMessage(convId, "user", userMessage, currentIntent, "[]");
        conversationService.recordMessage(convId, "model", reply, currentIntent, coursesJson);

        return new ChatbotMessageResponse(
                convId,
                reply,
                currentIntent.name(),
                verifiedCourses,
                suggestions
        );
    }

    private boolean isSearchIntent(ChatbotIntent intent) {
        return intent == ChatbotIntent.SEARCH_COURSE
                || intent == ChatbotIntent.COURSE_RECOMMENDATION
                || intent == ChatbotIntent.PRICE_INQUIRY
                || intent == ChatbotIntent.CORRECT_SEARCH
                || intent == ChatbotIntent.COURSE_DETAIL;
    }

    private List<String> generateSmartSuggestions(
            ChatbotIntent intent,
            CourseSearchFilter filters,
            List<ChatbotCourseCardDto> courses
    ) {
        List<String> list = new ArrayList<>();
        SubjectCode subject = filters != null ? filters.subject() : null;
        Integer grade = filters != null ? filters.grade() : null;

        if (courses.isEmpty() && subject != null) {
            if (grade != null && grade == 10 && subject == SubjectCode.MATH) {
                list.add("Tìm khóa học Toán lớp 9");
                list.add("Tìm khóa học Toán lớp 12");
                list.add("Học phí và hình thức thanh toán");
            } else if (grade != null && grade == 6) {
                list.add("Khóa học Toán 6 miễn phí có tài liệu gì đính kèm?");
                list.add("Cách đăng ký học thử Toán 6");
            } else {
                list.add("Tìm khóa học " + subject.getDisplayName() + " lớp 9");
                list.add("Tìm khóa học " + subject.getDisplayName() + " lớp 12");
                list.add("Chính sách thanh toán VietQR Napas 24/7");
            }
        } else if (!courses.isEmpty()) {
            list.add("Học phí và hình thức thanh toán VietQR");
            list.add("Khóa học có bài giảng xem thử miễn phí không?");
            if (grade != null && grade == 12) {
                list.add("Combo các môn luyện thi tốt nghiệp THPT");
            } else if (grade != null && grade == 9) {
                list.add("Khóa học ôn thi vào lớp 10 chuyên");
            } else {
                list.add("Chính sách cấp chứng chỉ số sau khi hoàn thành");
            }
        } else {
            list.add("Danh mục các khóa học hiện có");
            list.add("Tư vấn khóa học môn Toán lớp 6");
            list.add("Khóa học luyện thi vào lớp 10");
            list.add("Bộ môn luyện thi tốt nghiệp THPT");
            list.add("Học phí và hình thức thanh toán");
        }

        return list;
    }
}

