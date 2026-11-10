package com.beeacademy.backend.infrastructure.common.adapter;

import com.beeacademy.backend.api.ai.dto.AiChatRequest;
import com.beeacademy.backend.api.ai.dto.ChatbotCourseCardDto;
import com.beeacademy.backend.domain.ai.ChatbotIntent;
import com.beeacademy.backend.domain.ai.CourseSearchFilter;
import com.beeacademy.backend.domain.ai.IntentAnalysisResult;
import com.beeacademy.backend.domain.ai.SubjectCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter tương tác với Google Gemini Flash API cho Trợ lý Tư vấn BeeBot.
 * Hỗ trợ 2 pha: Phân tích NLU ý định người dùng (Giai đoạn 1) và Sinh câu trả lời tư vấn (Giai đoạn 3).
 * Có động cơ NLU và bộ sinh lời tư vấn nội bộ chuẩn mực khi không có kết nối internet/API key.
 */
@Component
public class GeminiAiAdapter {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiAdapter.class);

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-2.5-flash}")
    private String modelName;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();

    public GeminiAiAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // =========================================================================
    // GIAI ĐOẠN 1: Phân tích Ý định & Trích xuất Điều kiện tìm kiếm (NLU)
    // =========================================================================

    public IntentAnalysisResult analyzeUserIntent(
            String userMessage,
            CourseSearchFilter currentFilters,
            List<AiChatRequest.ChatMessageDto> history
    ) {
        if (userMessage == null || userMessage.isBlank()) {
            return IntentAnalysisResult.of(ChatbotIntent.GENERAL_QUESTION, null, null, null, null, null, false);
        }

        String curSubjectStr = currentFilters != null && currentFilters.subject() != null
                ? currentFilters.subject().name() : "null";
        String curGradeStr = currentFilters != null && currentFilters.grade() != null
                ? currentFilters.grade().toString() : "null";

        String systemInstruction = String.format("""
                Bạn là bộ phận phân tích ý định ngôn ngữ tự nhiên (NLU) của trợ lý BeeBot (nền tảng giáo dục K-12 BeeAcademy).
                Nhiệm vụ: Phân tích câu nói của người dùng và trích xuất thành DUY NHẤT một JSON hợp lệ (không kèm markdown ```json).

                TRẠNG THÁI HIỆN TẠI TRONG PHIÊN HỘI THOẠI (Current Filters):
                - Môn học hiện tại: %s
                - Khối lớp hiện tại: %s

                SCHEMA JSON BẮT BUỘC TRẢ VỀ:
                {
                  "intent": "SEARCH_COURSE | COURSE_DETAIL | COURSE_RECOMMENDATION | PRICE_INQUIRY | GENERAL_QUESTION | CLARIFICATION | CORRECT_SEARCH",
                  "subject": "MATH | PHYSICS | CHEMISTRY | BIOLOGY | ENGLISH | LITERATURE | IT | null",
                  "grade": <số nguyên từ 6 đến 12 hoặc null>,
                  "learningGoal": <chuỗi mô tả mục tiêu hoặc null>,
                  "budget": <số nguyên VNĐ hoặc null>,
                  "sortBy": "PRICE_ASC | PRICE_DESC | null",
                  "isCorrection": <true nếu người dùng sửa/đính chính kết quả trước, false nếu không>
                }

                QUY TẮC NHẬN DIỆN MÔN HỌC (subject):
                - Toán, đại số, hình học -> "MATH"
                - Vật lý, lý, dao động -> "PHYSICS"
                - Hóa học, hóa, este, lipit -> "CHEMISTRY"
                - Tiếng Anh, anh văn, english -> "ENGLISH"
                - Ngữ văn, văn -> "LITERATURE"
                - Tin học, lập trình, python -> "IT"

                QUY TẮC ĐÍNH CHÍNH (CORRECT_SEARCH & isCorrection):
                - Khi người dùng nói "Toán chứ không phải Tiếng Anh", "nhầm rồi", "sửa lại", "không phải lớp 10, tôi muốn lớp 11":
                  + intent = "CORRECT_SEARCH", isCorrection = true
                  + Bóc tách môn học hoặc khối lớp người dùng MUỐN (VD: "Toán chứ không phải Tiếng Anh" -> subject: "MATH").
                """, curSubjectStr, curGradeStr);

        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String jsonResponse = executeGeminiJsonCall(modelName, systemInstruction, userMessage, history);
                if (jsonResponse != null && !jsonResponse.isBlank()) {
                    JsonNode root = objectMapper.readTree(cleanJsonString(jsonResponse));
                    ChatbotIntent intent = ChatbotIntent.fromString(root.path("intent").asText("GENERAL_QUESTION"));
                    SubjectCode subject = SubjectCode.fromString(root.path("subject").asText(null));
                    Integer grade = root.hasNonNull("grade") ? root.path("grade").asInt() : null;
                    String goal = root.hasNonNull("learningGoal") ? root.path("learningGoal").asText() : null;
                    Integer budget = root.hasNonNull("budget") ? root.path("budget").asInt() : null;
                    String sortBy = root.hasNonNull("sortBy") ? root.path("sortBy").asText() : null;
                    boolean isCorrection = root.path("isCorrection").asBoolean(intent == ChatbotIntent.CORRECT_SEARCH);

                    return IntentAnalysisResult.of(intent, subject, grade, goal, budget, sortBy, isCorrection);
                }
            } catch (Exception e) {
                log.warn("Gemini NLU call failed: {}, falling back to internal rule-based NLU", e.getMessage());
            }
        }

        return fallbackNlu(userMessage, currentFilters);
    }

    // =========================================================================
    // GIAI ĐOẠN 3: Sinh Câu Trả Lời Tư Vấn (Advisory Reply)
    // =========================================================================

    public String generateAdvisoryReply(
            String userMessage,
            ChatbotIntent intent,
            CourseSearchFilter filters,
            List<ChatbotCourseCardDto> verifiedCourses,
            String courseKnowledge,
            List<AiChatRequest.ChatMessageDto> history
    ) {
        String systemInstruction = String.format("""
                Bạn là BeeBot — Chuyên viên Tư vấn Tuyển sinh & Khóa học trực tuyến của nền tảng BeeAcademy (K-12).
                Nhiệm vụ: Giải đáp và đưa ra lời tư vấn ngắn gọn, sư phạm, chuyên nghiệp cho Quý phụ huynh hoặc học sinh.

                DƯỚI ĐÂY LÀ DỮ LIỆU THỰC TẾ ĐƯỢC BACKEND XÁC THỰC TỪ DATABASE:
                %s

                QUY TẮC BẮT BUỘC:
                1. CHỈ TƯ VẤN DỰA TRÊN DỮ LIỆU THỰC TẾ ĐƯỢC BACKEND CUNG CẤP Ở TRÊN.
                2. NẾU DATABASE BÁO KHÔNG CÓ KHÓA HỌC: Bạn phải thông báo rõ ràng là hiện tại BeeAcademy chưa có khóa học cho yêu cầu này. TUYỆT ĐỐI KHÔNG lấy khóa học môn khác ra thay thế! Có thể gợi ý các khối lớp hoặc khóa học cùng môn đang có.
                3. KHÔNG CẦN TỰ CHÈN LINK MARKDOWN HAY VIẾT LẠI GIÁ BÁN vì hệ thống Frontend sẽ tự động hiển thị thẻ khóa học (Course Card) bên dưới câu trả lời của bạn.
                4. Xưng hô: "BeeBot" và "Quý phụ huynh/em học sinh", giữ văn phong lịch sự, ấm áp và chuyên nghiệp.
                """, courseKnowledge != null ? courseKnowledge : "");

        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String responseText = executeGeminiCall(modelName, systemInstruction, userMessage, history);
                if (responseText != null && !responseText.isBlank()) {
                    return responseText;
                }
            } catch (Exception e) {
                log.warn("Gemini Advisory call failed: {}, falling back to rule-based reply", e.getMessage());
            }
        }

        return generateFallbackReply(userMessage, intent, filters, verifiedCourses);
    }

    // =========================================================================
    // Fallback NLU & Local Response Generator (Đảm bảo hoạt động 100% offline)
    // =========================================================================

    private IntentAnalysisResult fallbackNlu(String userMessage, CourseSearchFilter currentFilters) {
        String lower = userMessage.toLowerCase();

        boolean isCorrection = lower.contains("chứ không phải")
                || lower.contains("không phải")
                || lower.contains("nhầm")
                || lower.contains("sửa lại")
                || lower.contains("thay vì");

        ChatbotIntent intent;
        if (isCorrection) {
            intent = ChatbotIntent.CORRECT_SEARCH;
        } else if (lower.contains("học phí") || lower.contains("bao nhiêu tiền") || lower.contains("giá") || lower.contains("thanh toán") || lower.contains("vietqr")) {
            intent = ChatbotIntent.PRICE_INQUIRY;
        } else if (lower.contains("tư vấn") || lower.contains("lộ trình") || lower.contains("chọn khóa") || lower.contains("nên học") || lower.contains("mất gốc")) {
            intent = ChatbotIntent.COURSE_RECOMMENDATION;
        } else if (lower.contains("chi tiết") || lower.contains("đề cương") || lower.contains("nội dung khóa")) {
            intent = ChatbotIntent.COURSE_DETAIL;
        } else if (lower.contains("tìm") || lower.contains("khóa học") || lower.contains("có khóa") || lower.contains("lớp")) {
            intent = ChatbotIntent.SEARCH_COURSE;
        } else {
            intent = ChatbotIntent.GENERAL_QUESTION;
        }

        // Bóc tách môn học: xử lý mệnh đề phủ định "Toán chứ không phải Tiếng Anh"
        SubjectCode subject = extractSubjectWithNegation(lower);

        // Bóc tách khối lớp
        Integer grade = extractGrade(lower);

        return IntentAnalysisResult.of(intent, subject, grade, null, null, null, isCorrection);
    }

    private SubjectCode extractSubjectWithNegation(String lower) {
        // Nếu có "chứ không phải [môn B]"
        if (lower.contains("chứ không phải") || lower.contains("thay vì")) {
            String[] parts = lower.split("chứ không phải|thay vì");
            if (parts.length >= 1) {
                SubjectCode targetSubject = detectSubjectInString(parts[0]);
                if (targetSubject != null) return targetSubject;
            }
        }

        return detectSubjectInString(lower);
    }

    private SubjectCode detectSubjectInString(String s) {
        if (s.contains("toán") || s.contains("đại số") || s.contains("hình học")) return SubjectCode.MATH;
        if (s.contains("vật lý") || s.contains("vật lí") || s.contains("lý") || s.contains("dao động")) return SubjectCode.PHYSICS;
        if (s.contains("hóa học") || s.contains("hóa") || s.contains("este") || s.contains("lipit")) return SubjectCode.CHEMISTRY;
        if (s.contains("tiếng anh") || s.contains("anh văn") || s.contains("english")) return SubjectCode.ENGLISH;
        if (s.contains("ngữ văn") || s.contains("văn học") || s.contains("văn")) return SubjectCode.LITERATURE;
        if (s.contains("tin học") || s.contains("lập trình") || s.contains("python") || s.contains("code")) return SubjectCode.IT;
        if (s.contains("sinh học") || s.contains("sinh")) return SubjectCode.BIOLOGY;
        return null;
    }

    private Integer extractGrade(String lower) {
        // Nếu người dùng nói "không phải lớp 10, tôi muốn lớp 11"
        if (lower.contains("không phải lớp") && lower.contains("muốn lớp")) {
            Pattern p = Pattern.compile("muốn lớp\\s*(1[0-2]|[6-9])");
            Matcher m = p.matcher(lower);
            if (m.find()) return Integer.parseInt(m.group(1));
        }

        if (lower.contains("vào 10") || lower.contains("vào lớp 10") || lower.contains("thi cấp 3")) {
            return 9;
        }
        if (lower.contains("tốt nghiệp") || lower.contains("thpt") || lower.contains("đại học")) {
            return 12;
        }

        Pattern p = Pattern.compile("(lớp|khoi|khối|toán|lý|hóa|anh)\\s*(1[0-2]|[6-9])");
        Matcher m = p.matcher(lower);
        if (m.find()) {
            return Integer.parseInt(m.group(2));
        }

        return null;
    }

    private String generateFallbackReply(
            String userMessage,
            ChatbotIntent intent,
            CourseSearchFilter filters,
            List<ChatbotCourseCardDto> verifiedCourses
    ) {
        String subjectLabel = filters != null && filters.subject() != null ? filters.subject().getDisplayName() : "khóa học";
        String gradeLabel = filters != null && filters.grade() != null ? "lớp " + filters.grade() : "";

        if (verifiedCourses == null || verifiedCourses.isEmpty()) {
            if (filters != null && filters.subject() != null && filters.grade() != null) {
                return String.format(
                        "Hiện tại BeeAcademy chưa có khóa học %s %s phù hợp trong hệ thống.\n\n" +
                        "Quý phụ huynh và các em học sinh có thể tham khảo các khóa học %s ở các khối lớp khác, hoặc theo dõi thông báo khi BeeAcademy ra mắt chương trình mới.",
                        subjectLabel, gradeLabel, subjectLabel
                );
            }
            return "Hiện tại BeeAcademy chưa tìm thấy khóa học phù hợp với yêu cầu trên. Quý phụ huynh hoặc học sinh vui lòng cho biết thêm khối lớp (từ lớp 6 đến lớp 12) hoặc môn học quan tâm để BeeBot hỗ trợ tra cứu chính xác nhất.";
        }

        if (intent == ChatbotIntent.CORRECT_SEARCH) {
            return String.format(
                    "BeeBot đã cập nhật lại điều kiện tìm kiếm môn %s %s theo đúng yêu cầu của Quý khách. Dưới đây là các khóa học phù hợp hiện có trên hệ thống:",
                    subjectLabel, gradeLabel
            );
        }

        if (intent == ChatbotIntent.PRICE_INQUIRY) {
            return String.format(
                    "Về học phí các khóa học %s %s, BeeAcademy niêm yết công khai và hỗ trợ thanh toán linh hoạt qua VietQR PayOS kích hoạt phòng học ngay lập tức. Mời Quý phụ huynh và các em học sinh tham khảo bên dưới:",
                    subjectLabel, gradeLabel
            );
        }

        return String.format(
                "BeeAcademy hiện có các khóa học %s %s phù hợp với mục tiêu học tập của Quý khách. Thông tin chi tiết đề cương và học phí được hiển thị bên dưới:",
                subjectLabel, gradeLabel
        );
    }

    // =========================================================================
    // Gọi HTTP Gemini API
    // =========================================================================

    private String executeGeminiJsonCall(String targetModel, String systemInstruction, String userMessage, List<AiChatRequest.ChatMessageDto> history) {
        try {
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    targetModel, apiKey
            );

            List<Map<String, Object>> contents = buildContentsPayload(userMessage, history);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("system_instruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));
            payload.put("contents", contents);
            payload.put("generationConfig", Map.of("response_mime_type", "application/json"));

            String requestBody = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(12))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return extractTextFromResponse(response.body());
            }
        } catch (Exception e) {
            log.warn("Lỗi Gemini JSON API call: {}", e.getMessage());
        }
        return null;
    }

    private String executeGeminiCall(String targetModel, String systemInstruction, String userMessage, List<AiChatRequest.ChatMessageDto> history) {
        try {
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    targetModel, apiKey
            );

            List<Map<String, Object>> contents = buildContentsPayload(userMessage, history);
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("system_instruction", Map.of("parts", List.of(Map.of("text", systemInstruction))));
            payload.put("contents", contents);

            String requestBody = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return extractTextFromResponse(response.body());
            }
        } catch (Exception e) {
            log.error("Lỗi Gemini API call: {}", e.getMessage());
        }
        return null;
    }

    private List<Map<String, Object>> buildContentsPayload(String userMessage, List<AiChatRequest.ChatMessageDto> history) {
        List<Map<String, Object>> contents = new ArrayList<>();
        if (history != null) {
            int start = Math.max(0, history.size() - 6);
            for (int i = start; i < history.size(); i++) {
                AiChatRequest.ChatMessageDto msg = history.get(i);
                contents.add(Map.of(
                        "role", "model".equalsIgnoreCase(msg.role()) ? "model" : "user",
                        "parts", List.of(Map.of("text", msg.text()))
                ));
            }
        }
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userMessage))
        ));
        return contents;
    }

    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode partsNode = root.path("candidates").get(0).path("content").path("parts");
            if (partsNode.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode part : partsNode) {
                    if (part.has("text") && !part.path("thought").asBoolean(false)) {
                        sb.append(part.path("text").asText());
                    }
                }
                if (!sb.isEmpty()) {
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi phân tích response từ Gemini: {}", e.getMessage());
        }
        return null;
    }

    private String cleanJsonString(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        if (s.startsWith("```json")) {
            s = s.substring(7);
        } else if (s.startsWith("```")) {
            s = s.substring(3);
        }
        if (s.endsWith("```")) {
            s = s.substring(0, s.length() - 3);
        }
        return s.trim();
    }

    // =========================================================================
    // Backward Compatibility method
    // =========================================================================
    public String chatAdvisor(String userMessage, String context, String courseKnowledge, List<AiChatRequest.ChatMessageDto> history) {
        return generateAdvisoryReply(userMessage, ChatbotIntent.GENERAL_QUESTION, CourseSearchFilter.empty(), Collections.emptyList(), courseKnowledge, history);
    }
}
