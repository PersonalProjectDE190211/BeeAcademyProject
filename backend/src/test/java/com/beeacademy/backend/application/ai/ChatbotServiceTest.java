package com.beeacademy.backend.application.ai;

import com.beeacademy.backend.api.ai.dto.ChatbotCourseCardDto;
import com.beeacademy.backend.api.ai.dto.ChatbotMessageResponse;
import com.beeacademy.backend.application.ai.service.ChatbotService;
import com.beeacademy.backend.application.ai.service.ConversationService;
import com.beeacademy.backend.application.ai.service.CourseSearchService;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.course.*;
import com.beeacademy.backend.domain.ai.*;
import com.beeacademy.backend.infrastructure.common.adapter.GeminiAiAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@DisplayName("ChatbotService & Multi-Turn Conversational Search Unit Tests")
class ChatbotServiceTest {

    private CourseRepositoryPort courseRepository;
    private CategoryRepositoryPort categoryRepository;
    private GeminiAiAdapter geminiAiAdapter;
    private ConversationService conversationService;
    private CourseSearchService courseSearchService;
    private ChatbotService chatbotService;
    private ObjectMapper objectMapper;

    private UUID mathCategoryId;
    private UUID englishCategoryId;
    private Course math6Course;
    private Course math9Course;
    private Course english10Course;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        courseRepository = Mockito.mock(CourseRepositoryPort.class);
        categoryRepository = Mockito.mock(CategoryRepositoryPort.class);
        conversationService = Mockito.mock(ConversationService.class);

        // Khởi tạo GeminiAiAdapter với ObjectMapper thật để test fallback NLU
        geminiAiAdapter = new GeminiAiAdapter(objectMapper);
        courseSearchService = new CourseSearchService(courseRepository, categoryRepository);
        chatbotService = new ChatbotService(geminiAiAdapter, courseSearchService, conversationService, objectMapper);

        mathCategoryId = UUID.randomUUID();
        englishCategoryId = UUID.randomUUID();

        Category mathCat = new Category(mathCategoryId, "Toán học", "toan-hoc", "Toán K-12", "Calculator", 1, true);
        Category englishCat = new Category(englishCategoryId, "Tiếng Anh", "tieng-anh", "Tiếng Anh K-12", "Globe", 2, true);

        when(categoryRepository.findAllActive()).thenReturn(List.of(mathCat, englishCat));

        UUID teacherId = UUID.randomUUID();

        math6Course = new Course(
                UUID.randomUUID(), teacherId, mathCategoryId,
                "Toán 6 — Khởi Đầu Vững Chắc: Số Học & Hình Học Trực Quan",
                null, "Mô tả Toán 6", "https://img.com/math6.jpg",
                MoneyVnd.ZERO, CourseStatus.PUBLISHED, List.of(6),
                3, 6, 10100, BigDecimal.valueOf(5.0), 35,
                Instant.now(), Instant.now(), Instant.now(),
                new ArrayList<>(), new ArrayList<>()
        );

        math9Course = new Course(
                UUID.randomUUID(), teacherId, mathCategoryId,
                "Toán 9 — Chiến Thuật Luyện Thi Vào Lớp 10 Chuyên & Công Lập",
                null, "Mô tả Toán 9", "https://img.com/math9.jpg",
                MoneyVnd.of(220000), CourseStatus.PUBLISHED, List.of(9),
                2, 4, 6400, BigDecimal.valueOf(4.9), 22,
                Instant.now(), Instant.now(), Instant.now(),
                new ArrayList<>(), new ArrayList<>()
        );

        english10Course = new Course(
                UUID.randomUUID(), teacherId, englishCategoryId,
                "Tiếng Anh 10 — Làm Chủ Ngữ Pháp B1–B2 & Từ Vựng Chuyên Đề",
                null, "Mô tả Tiếng Anh 10", "https://img.com/eng10.jpg",
                MoneyVnd.of(180000), CourseStatus.PUBLISHED, List.of(10),
                2, 4, 6800, BigDecimal.valueOf(4.8), 12,
                Instant.now(), Instant.now(), Instant.now(),
                new ArrayList<>(), new ArrayList<>()
        );
    }

    @Test
    @DisplayName("Giai đoạn 2 & 4: Tìm Toán 10 trả về danh sách rỗng, TUYỆT ĐỐI không lấy Tiếng Anh 10 thay thế")
    void searchMathGrade10_ReturnsEmptyList_NeverReturnsEnglish() {
        // Giả lập DB chỉ có Toán 6, Toán 9 và Tiếng Anh 10 (chưa có Toán 10)
        when(courseRepository.findPublishedCourses(eq(mathCategoryId), eq(10)))
                .thenReturn(Collections.emptyList());

        CourseSearchFilter filter = new CourseSearchFilter(SubjectCode.MATH, 10, null, null, null);
        List<ChatbotCourseCardDto> results = courseSearchService.searchAndVerify(filter);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Giai đoạn 2 & 4: Tìm Toán 6 trả về đúng khóa học Toán 6 với giá Miễn phí (0đ)")
    void searchMathGrade6_ReturnsMath6WithZeroPrice() {
        when(courseRepository.findPublishedCourses(eq(mathCategoryId), eq(6)))
                .thenReturn(List.of(math6Course));

        CourseSearchFilter filter = new CourseSearchFilter(SubjectCode.MATH, 6, null, null, null);
        List<ChatbotCourseCardDto> results = courseSearchService.searchAndVerify(filter);

        assertThat(results).hasSize(1);
        ChatbotCourseCardDto card = results.get(0);
        assertThat(card.title()).contains("Toán 6");
        assertThat(card.price()).isEqualTo(0);
        assertThat(card.grade()).isEqualTo(6);
        assertThat(card.detailUrl()).isEqualTo("/courses/" + math6Course.getId());
    }

    @Test
    @DisplayName("State Merging: Khi người dùng đính chính 'Toán chứ không phải Tiếng Anh', giữ nguyên grade 10 và cập nhật subject MATH")
    void stateMerging_CorrectionKeepsGradeAndUpdatesSubject() {
        // Ngữ cảnh trước đó: người dùng đã hỏi lớp 10
        CourseSearchFilter initialFilter = new CourseSearchFilter(null, 10, null, null, null);

        // NLU phân tích câu "Toán chứ không phải Tiếng Anh"
        IntentAnalysisResult nlu = geminiAiAdapter.analyzeUserIntent(
                "Toán chứ không phải Tiếng Anh",
                initialFilter,
                Collections.emptyList()
        );

        assertThat(nlu.intent()).isEqualTo(ChatbotIntent.CORRECT_SEARCH);
        assertThat(nlu.subject()).isEqualTo(SubjectCode.MATH);

        // Hợp nhất bộ lọc
        CourseSearchFilter merged = initialFilter.mergeWith(nlu.toFilter());
        assertThat(merged.subject()).isEqualTo(SubjectCode.MATH);
        assertThat(merged.grade()).isEqualTo(10);
    }

    @Test
    @DisplayName("State Merging: Khi người dùng đổi 'Tôi muốn tìm Toán lớp 9' sau lượt Toán 6, cập nhật grade 9 và giữ môn MATH")
    void stateMerging_GradeUpdatePreservesSubject() {
        CourseSearchFilter initialFilter = new CourseSearchFilter(SubjectCode.MATH, 6, null, null, null);

        IntentAnalysisResult nlu = geminiAiAdapter.analyzeUserIntent(
                "Tôi muốn tìm Toán lớp 9",
                initialFilter,
                Collections.emptyList()
        );

        assertThat(nlu.grade()).isEqualTo(9);
        assertThat(nlu.subject()).isEqualTo(SubjectCode.MATH);

        CourseSearchFilter merged = initialFilter.mergeWith(nlu.toFilter());
        assertThat(merged.subject()).isEqualTo(SubjectCode.MATH);
        assertThat(merged.grade()).isEqualTo(9);
    }

    @Test
    @DisplayName("Luồng hội thoại 4 giai đoạn: Kiểm tra xử lý hoàn chỉnh với ConversationContext")
    void processMessage_CompleteFlow() {
        UUID convId = UUID.randomUUID();
        ConversationContext initialContext = ConversationContext.init(convId, null);

        when(conversationService.getOrCreateContext(any(), any())).thenReturn(initialContext);
        when(courseRepository.findPublishedCourses(eq(mathCategoryId), eq(9)))
                .thenReturn(List.of(math9Course));

        ChatbotMessageResponse response = chatbotService.processMessage(convId, null, "Tôi muốn tìm Toán lớp 9");

        assertThat(response.conversationId()).isEqualTo(convId);
        assertThat(response.courses()).hasSize(1);
        assertThat(response.courses().get(0).title()).contains("Toán 9");
        assertThat(response.reply()).isNotEmpty();
        assertThat(response.suggestions()).isNotEmpty();
    }
}

