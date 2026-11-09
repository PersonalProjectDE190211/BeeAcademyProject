package com.beeacademy.backend.infrastructure.common.adapter;

import com.beeacademy.backend.api.ai.dto.AiChatRequest;
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

/**
 * Adapter tương tác với Google Gemini Flash API phục vụ Trợ lý Tư vấn Tuyển sinh & Khóa học BeeBot (UC22).
 * Tự động kết nối Google Gemini API với tri thức khóa học K-12, và có động cơ tư vấn nội bộ dự phòng.
 */
@Component
public class GeminiAiAdapter {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiAdapter.class);

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-3.5-flash-lite}")
    private String modelName;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();

    public GeminiAiAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * UC22: Tư vấn tuyển sinh & giải đáp thắc mắc khóa học K-12 cho Phụ huynh và Học sinh
     */
    public String chatAdvisor(String userMessage, String context, String courseKnowledge, List<AiChatRequest.ChatMessageDto> history) {
        String systemInstruction = String.format("""
                Bạn là BeeBot — Chuyên viên Tư vấn Tuyển sinh & Khóa học trực tuyến của nền tảng giáo dục BeeAcademy (K-12).
                Nhiệm vụ trọng tâm của bạn là giải đáp thắc mắc, tư vấn chọn khóa học phù hợp cho PHỤ HUYNH và HỌC SINH dựa trên thông tin về khối lớp, học lực, mục tiêu hoặc câu hỏi mà người dùng đưa ra.

                DƯỚI ĐÂY LÀ DỮ LIỆU CÁC KHÓA HỌC HIỆN CÓ CỦA BEEACADEMY:
                %s

                QUY TẮC BẮT BUỘC VỀ VĂN PHONG VÀ BỐ CỤC:
                1. TUYỆT ĐỐI KHÔNG SỬ DỤNG CÁC BIỂU TƯỢNG EMOJI VÔ NGHĨA: Giữ văn phong sư phạm chuẩn mực, chuyên nghiệp, lịch sự và bố cục mạch lạc.
                2. BẮT BUỘC chèn đường link dạng Markdown [Xem chi tiết khóa học {tên}](/courses/{id}) ở ngay bên dưới mỗi khóa học được đề cập để người dùng có thể nhấp chuột trực tiếp vào xem khóa học.
                3. CHỈ TƯ VẤN CÁC KHÓA HỌC THỰC TẾ TRONG DANH SÁCH TRÊN: Không bịa đặt hoặc giới thiệu khóa học ngoài danh mục.
                4. Khi người dùng hỏi chung hoặc hỏi "tư vấn các khóa học có của website": Liệt kê rõ ràng và ngắn gọn các khóa học theo từng khối lớp kèm học phí (đặc biệt nhấn mạnh khóa Toán 6 là HOÀN TOÀN MIỄN PHÍ).
                5. Khi phụ huynh hoặc học sinh hỏi nên học khóa nào: Phân tích dựa trên khối lớp hoặc nhu cầu họ đưa ra để gợi ý khóa học phù hợp nhất.
                6. Về học phí & thanh toán: Học phí minh bạch, thanh toán tự động qua VietQR PayOS (Napas 24/7) hoặc VNPay được kích hoạt phòng học ngay lập tức.
                7. Xưng hô: "BeeBot" và "Quý phụ huynh/em học sinh".
                """, courseKnowledge != null ? courseKnowledge : "");

        if (apiKey != null && !apiKey.isBlank()) {
            try {
                String responseText = callGeminiWithFallback(systemInstruction, userMessage, history);
                if (responseText != null && !responseText.isBlank()) {
                    return responseText;
                }
            } catch (Exception e) {
                log.warn("Gemini Live API call failed, falling back to local advisor engine: {}", e.getMessage());
            }
        }

        return generateLocalAdvisorResponse(userMessage, context, courseKnowledge);
    }

    private String callGeminiWithFallback(String systemInstruction, String userMessage, List<AiChatRequest.ChatMessageDto> history) {
        String result = executeGeminiCall(modelName, systemInstruction, userMessage, history);
        if (result != null && !result.isBlank()) {
            return result;
        }

        // Tự động thử model gemini-3.5-flash-lite nếu model chính bị 503 hoặc quá tải
        if (!"gemini-3.5-flash-lite".equalsIgnoreCase(modelName)) {
            log.info("Thử lại Gemini API với model gemini-3.5-flash-lite...");
            return executeGeminiCall("gemini-3.5-flash-lite", systemInstruction, userMessage, history);
        }

        return null;
    }

    private String executeGeminiCall(String targetModel, String systemInstruction, String userMessage, List<AiChatRequest.ChatMessageDto> history) {
        try {
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    targetModel, apiKey
            );

            List<Map<String, Object>> contents = new ArrayList<>();
            if (history != null) {
                for (AiChatRequest.ChatMessageDto msg : history) {
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

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("system_instruction", Map.of(
                    "parts", List.of(Map.of("text", systemInstruction))
            ));
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
                JsonNode root = objectMapper.readTree(response.body());
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
                    // Dự phòng nếu part không có thought tag
                    for (int i = partsNode.size() - 1; i >= 0; i--) {
                        String t = partsNode.get(i).path("text").asText("");
                        if (!t.isBlank()) return t;
                    }
                }
            } else {
                log.warn("Gemini API ({}) error code: {} body: {}", targetModel, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Gemini API ({}) request error: {}", targetModel, e.getMessage());
        }
        return null;
    }

    private String generateLocalAdvisorResponse(String message, String context, String courseKnowledge) {
        String msgLower = message.toLowerCase();

        if (msgLower.contains("toán 6") || msgLower.contains("lớp 6")) {
            return "Chào Quý phụ huynh và em học sinh,\n\n" +
                    "Đối với khối lớp 6, BeeAcademy hiện cung cấp chương trình học nền tảng:\n\n" +
                    "**Khóa học: Toán 6 — Khởi Đầu Vững Chắc: Số Học & Hình Học Trực Quan**\n" +
                    "- Khối lớp: Lớp 6\n" +
                    "- Học phí: Miễn phí (0 VNĐ)\n" +
                    "- Đánh giá: 5.0/5.0 (35 lượt đánh giá)\n" +
                    "- Quy mô: 3 chương, 6 bài giảng video và 7 tài liệu PDF chuẩn chỉnh\n" +
                    "- Nội dung trọng tâm: Tập hợp, Quan hệ chia hết, Số nguyên tố, Hình học phẳng trực quan\n" +
                    "- Đối tượng phù hợp: Học sinh lớp 6 muốn củng cố nền tảng, chống mất gốc môn Toán khi chuyển cấp từ tiểu học lên THCS\n\n" +
                    "Đường dẫn khóa học:\n" +
                    "[Xem chi tiết khóa học Toán 6](/courses/619f46b8-96bb-43ba-801c-a6ef913f6c54)\n\n" +
                    "Quý phụ huynh hoặc học sinh có thể nhấn vào liên kết trên để đăng ký học ngay mà không mất bất kỳ chi phí nào.";
        }

        if (msgLower.contains("toán 9") || msgLower.contains("lớp 9") || msgLower.contains("vào 10")) {
            return "Chào Quý phụ huynh và em học sinh,\n\n" +
                    "Đối với mục tiêu ôn thi vào lớp 10, BeeAcademy hiện có chương trình trọng tâm:\n\n" +
                    "**Khóa học: Toán 9 — Chiến Thuật Luyện Thi Vào Lớp 10 Chuyên & Công Lập**\n" +
                    "- Khối lớp: Lớp 9\n" +
                    "- Học phí: 220.000 VNĐ\n" +
                    "- Đánh giá: 4.9/5.0 (22 lượt đánh giá)\n" +
                    "- Quy mô: 2 chương chuyên sâu, 4 bài giảng video kèm phiếu bài tập rèn luyện\n" +
                    "- Nội dung: Hệ phương trình, Bất đẳng thức Cô-si và Hình học quỹ tích phân loại thí sinh\n" +
                    "- Mục tiêu: Giúp học sinh đạt mục tiêu từ 8.5 đến 9.5+ điểm trong kỳ thi tuyển sinh vào 10\n\n" +
                    "Đường dẫn khóa học:\n" +
                    "[Xem chi tiết khóa học Toán 9](/courses/de892ca3-ae06-4d8e-9a8f-adec4313684d)";
        }

        if (msgLower.contains("lớp 12") || msgLower.contains("tốt nghiệp") || msgLower.contains("thpt") || msgLower.contains("đại học")) {
            return "Chào Quý phụ huynh và em học sinh,\n\n" +
                    "Phục vụ kỳ thi tốt nghiệp THPT và xét tuyển Đại học (Khối 12), BeeAcademy cung cấp bộ 3 khóa học chủ lực:\n\n" +
                    "1. **Toán 12 — Chinh Phục Khảo Sát Hàm Số, Đạo Hàm & Tích Phân Ứng Dụng**\n" +
                    "   - Học phí: 299.000 VNĐ (5.0/5.0 - 28 đánh giá)\n" +
                    "   - Kỹ năng giải nhanh 50 câu trắc nghiệm từ nhận biết đến vận dụng cao\n" +
                    "   - [Xem chi tiết khóa học Toán 12](/courses/67d48484-7fed-40f2-9f1d-2f576f8a6ca6)\n\n" +
                    "2. **Vật Lý 12 — Nắm Trọn Điểm 9+ Dao Động Cơ & Sóng Cơ Học**\n" +
                    "   - Học phí: 250.000 VNĐ (4.9/5.0 - 19 đánh giá)\n" +
                    "   - Hệ thống hóa công thức dao động điều hòa, con lắc lò xo và bài tập đồ thị thực chiến\n" +
                    "   - [Xem chi tiết khóa học Vật Lý 12](/courses/6cf4854f-ae2e-4210-846f-58cedbebbddc)\n\n" +
                    "3. **Hóa Học 12 — Chuyên Đề Este, Lipit & Hợp Chất Hữu Cơ Chứa Nitơ**\n" +
                    "   - Học phí: 199.000 VNĐ (4.9/5.0 - 14 đánh giá)\n" +
                    "   - Kỹ thuật dồn chất, giải nhanh bài tập chất béo và biện luận chuỗi phản ứng\n" +
                    "   - [Xem chi tiết khóa học Hóa Học 12](/courses/d35d21ba-4b7e-4a50-b877-44cd21d0eaa5)\n\n" +
                    "Học sinh có thể đăng ký theo môn riêng lẻ hoặc combo, thanh toán linh hoạt qua VietQR hoặc VNPay để kích hoạt phòng học ngay lập tức.";
        }

        if (msgLower.contains("tiếng anh") || msgLower.contains("lớp 10") || msgLower.contains("anh 10")) {
            return "Chào Quý phụ huynh và em học sinh,\n\n" +
                    "Dành cho học sinh khối 10 theo chương trình mới, BeeAcademy cung cấp khóa học:\n\n" +
                    "**Khóa học: Tiếng Anh 10 — Làm Chủ Ngữ Pháp B1–B2 & Từ Vựng Chuyên Đề**\n" +
                    "- Khối lớp: Lớp 10\n" +
                    "- Học phí: 180.000 VNĐ\n" +
                    "- Đánh giá: 4.8/5.0 (12 lượt đánh giá)\n" +
                    "- Nội dung: Các thì ngữ pháp, mệnh đề quan hệ, câu điều kiện và từ vựng theo chủ điểm bộ sách Global Success\n\n" +
                    "Đường dẫn khóa học:\n" +
                    "[Xem chi tiết khóa học Tiếng Anh 10](/courses/46b1e9a4-1688-4314-96aa-611921a85741)";
        }

        if (msgLower.contains("học phí") || msgLower.contains("thanh toán") || msgLower.contains("tiền") || msgLower.contains("giá")) {
            return "Chào Quý phụ huynh và các em học sinh,\n\n" +
                    "Về chính sách học phí và hình thức thanh toán tại BeeAcademy:\n\n" +
                    "1. **Chính sách học phí:**\n" +
                    "   - Khóa học Toán 6: Miễn phí 100% (0 VNĐ).\n" +
                    "   - Các khóa học chuyên đề lớp 9, 10, 12: Mức phí từ 180.000 VNĐ đến 299.000 VNĐ / khóa học trọn đời.\n\n" +
                    "2. **Hình thức thanh toán:**\n" +
                    "   - Chuyển khoản quét mã VietQR PayOS (hỗ trợ Napas 24/7 từ mọi ứng dụng ngân hàng).\n" +
                    "   - Cổng thanh toán VNPay.\n" +
                    "   - Hệ thống tự động kích hoạt phòng học ngay sau khi hoàn tất thanh toán (thời gian xử lý 3–5 giây).\n\n" +
                    "Quý phụ huynh có thể nhấn vào liên kết bên dưới để xem toàn bộ danh mục khóa học:\n" +
                    "[Xem danh mục tất cả khóa học](/catalog)";
        }

        if (msgLower.contains("phụ huynh") || msgLower.contains("theo dõi") || msgLower.contains("giám sát") || msgLower.contains("con")) {
            return "Kính chào Quý phụ huynh,\n\n" +
                    "BeeAcademy cung cấp Cổng Phụ Huynh (Parent Portal) phục vụ việc đồng hành cùng con:\n\n" +
                    "- **Liên kết tài khoản:** Phụ huynh tạo tài khoản và gửi mã mời liên kết 6 ký tự đến tài khoản của con.\n" +
                    "- **Theo dõi tiến độ:** Xem chi tiết tỷ lệ % hoàn thành bài giảng video, bảng điểm các bài thi trắc nghiệm Quiz.\n" +
                    "- **Báo cáo định kỳ:** Nhận thông báo học tập tự động giúp phụ huynh nắm rõ lộ trình của con.";
        }

        // Mặc định: Danh mục các khóa học hiện có
        return "Chào Quý phụ huynh và các em học sinh,\n\n" +
                "Dưới đây là danh sách các khóa học hiện có trên hệ thống BeeAcademy:\n\n" +
                "1. **Toán 6 — Khởi Đầu Vững Chắc: Số Học & Hình Học Trực Quan**\n" +
                "   - Khối lớp: Lớp 6\n" +
                "   - Học phí: Miễn phí (0 VNĐ)\n" +
                "   - Đánh giá: 5.0/5.0 (35 đánh giá)\n" +
                "   - Quy mô: 3 chương, 6 bài học, 7 tài liệu PDF\n" +
                "   - [Xem chi tiết khóa học Toán 6](/courses/619f46b8-96bb-43ba-801c-a6ef913f6c54)\n\n" +
                "2. **Toán 9 — Chiến Thuật Luyện Thi Vào Lớp 10 Chuyên & Công Lập**\n" +
                "   - Khối lớp: Lớp 9\n" +
                "   - Học phí: 220.000 VNĐ\n" +
                "   - Đánh giá: 4.9/5.0 (22 đánh giá)\n" +
                "   - Quy mô: 2 chương, 4 bài học, tài liệu giải nhanh hệ phương trình và hình học\n" +
                "   - [Xem chi tiết khóa học Toán 9](/courses/de892ca3-ae06-4d8e-9a8f-adec4313684d)\n\n" +
                "3. **Tiếng Anh 10 — Làm Chủ Ngữ Pháp B1–B2 & Từ Vựng Chuyên Đề**\n" +
                "   - Khối lớp: Lớp 10\n" +
                "   - Học phí: 180.000 VNĐ\n" +
                "   - Đánh giá: 4.8/5.0 (12 đánh giá)\n" +
                "   - Quy mô: 2 chương, 4 bài học bám sát sách Global Success\n" +
                "   - [Xem chi tiết khóa học Tiếng Anh 10](/courses/46b1e9a4-1688-4314-96aa-611921a85741)\n\n" +
                "4. **Toán 12 — Chinh Phục Khảo Sát Hàm Số, Đạo Hàm & Tích Phân Ứng Dụng**\n" +
                "   - Khối lớp: Lớp 12\n" +
                "   - Học phí: 299.000 VNĐ\n" +
                "   - Đánh giá: 5.0/5.0 (28 đánh giá)\n" +
                "   - Quy mô: 2 chương, 6 bài học, 50 câu trắc nghiệm vận dụng cao\n" +
                "   - [Xem chi tiết khóa học Toán 12](/courses/67d48484-7fed-40f2-9f1d-2f576f8a6ca6)\n\n" +
                "5. **Vật Lý 12 — Nắm Trọn Điểm 9+ Dao Động Cơ & Sóng Cơ Học**\n" +
                "   - Khối lớp: Lớp 12\n" +
                "   - Học phí: 250.000 VNĐ\n" +
                "   - Đánh giá: 4.9/5.0 (19 đánh giá)\n" +
                "   - Quy mô: 2 chương, 4 bài học chuyên sâu dao động và sóng cơ\n" +
                "   - [Xem chi tiết khóa học Vật Lý 12](/courses/6cf4854f-ae2e-4210-846f-58cedbebbddc)\n\n" +
                "6. **Hóa Học 12 — Chuyên Đề Este, Lipit & Hợp Chất Nitơ**\n" +
                "   - Khối lớp: Lớp 12\n" +
                "   - Học phí: 199.000 VNĐ\n" +
                "   - Đánh giá: 4.9/5.0 (14 đánh giá)\n" +
                "   - Quy mô: 2 chương, 4 bài học kỹ thuật dồn chất và chuỗi phản ứng\n" +
                "   - [Xem chi tiết khóa học Hóa Học 12](/courses/d35d21ba-4b7e-4a50-b877-44cd21d0eaa5)\n\n" +
                "Quý phụ huynh hoặc học sinh có thể nhấn trực tiếp vào liên kết bên dưới mỗi khóa học để xem đề cương chi tiết và học thử bài học mẫu.";
    }
}
