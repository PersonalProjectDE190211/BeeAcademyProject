package com.beeacademy.backend.application.ai.usecase;

import com.beeacademy.backend.api.ai.dto.AiChatRequest;
import com.beeacademy.backend.api.ai.dto.AiChatResponse;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.infrastructure.common.adapter.GeminiAiAdapter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Use Case UC22: Trợ lý AI BeeBot - Tư vấn tuyển sinh & khóa học K-12.
 */
@Service
public class ChatWithAiTutorUseCase {

    private final GeminiAiAdapter geminiAiAdapter;
    private final CourseRepositoryPort courseRepository;

    public ChatWithAiTutorUseCase(GeminiAiAdapter geminiAiAdapter, CourseRepositoryPort courseRepository) {
        this.geminiAiAdapter = geminiAiAdapter;
        this.courseRepository = courseRepository;
    }

    public AiChatResponse execute(AiChatRequest request) {
        String courseKnowledge = buildCourseKnowledge();

        String reply = geminiAiAdapter.chatAdvisor(
                request.message(),
                request.context(),
                courseKnowledge,
                request.history()
        );

        List<String> suggestions = generateFollowUpSuggestions(request.message());

        return new AiChatResponse(reply, suggestions);
    }

    private String buildCourseKnowledge() {
        try {
            List<Course> courses = courseRepository.findPublishedCourses(null, null);
            if (courses == null || courses.isEmpty()) {
                return "Website hiện có các khóa học: Toán 6 (Miễn phí 0đ), Toán 9 (Vào 10 - 220.000đ), Toán 12 (299.000đ), Vật Lý 12 (250.000đ), Hóa Học 12 (199.000đ), Tiếng Anh 10 (180.000đ).";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Danh sách các khóa học hiện có trên website BeeAcademy:\n");
            for (int i = 0; i < courses.size(); i++) {
                Course c = courses.get(i);
                String priceStr = (c.getPrice() == null || c.getPrice().isZero())
                        ? "MIỄN PHÍ (0 VNĐ)"
                        : c.getPrice().toString();
                sb.append(String.format("%d. Khóa học: %s\n   - Khối lớp: %s\n   - Học phí: %s\n   - Đánh giá: %.1f/5.0 (%d đánh giá)\n   - Đề cương: %d bài giảng\n   - Mô tả: %s\n   - Đường dẫn xem chi tiết: /courses/%s\n",
                        i + 1,
                        c.getTitle(),
                        c.getGrades() != null && !c.getGrades().isEmpty() ? c.getGrades().toString() : "K-12",
                        priceStr,
                        c.getAverageRating() != null ? c.getAverageRating().doubleValue() : 5.0,
                        c.getTotalReviews(),
                        c.getTotalLessons(),
                        c.getDescription() != null ? c.getDescription() : "",
                        c.getId()
                ));
            }
            sb.append("\nQuy định: Khi tư vấn hoặc giới thiệu khóa học, luôn đặt đường link markdown [Xem chi tiết khóa học](/courses/{id}) ở cuối mỗi mục khóa học để người dùng có thể nhấp vào xem.");
            return sb.toString();
        } catch (Exception e) {
            return "Khóa học gồm: Toán 6 (Miễn phí 0đ), Toán 9 (220.000đ), Toán 12 (299.000đ), Vật Lý 12 (250.000đ), Hóa 12 (199.000đ), Tiếng Anh 10 (180.000đ).";
        }
    }

    private List<String> generateFollowUpSuggestions(String userMessage) {
        String lower = userMessage.toLowerCase();
        List<String> list = new ArrayList<>();

        if (lower.contains("lớp 6") || lower.contains("toán 6")) {
            list.add("Khóa học Toán 6 miễn phí có tài liệu gì đính kèm?");
            list.add("Học sinh mất gốc lớp 6 nên học theo lộ trình nào?");
            list.add("Cách đăng ký học thử bài 1 Toán 6");
        } else if (lower.contains("lớp 12") || lower.contains("đại học") || lower.contains("thpt") || lower.contains("tốt nghiệp")) {
            list.add("Combo 3 môn Toán - Lý - Hóa 12 luyện thi tốt nghiệp");
            list.add("Khóa Toán 12 có dạy phương pháp Casio giải nhanh không?");
            list.add("Học phí và hình thức thanh toán VietQR");
        } else if (lower.contains("lớp 9") || lower.contains("vào 10")) {
            list.add("Khóa Toán 9 có bài tập phân loại điểm 9, 10 không?");
            list.add("Thời gian học một khóa Toán 9 là bao lâu?");
            list.add("Học sinh lớp 9 nên bắt đầu ôn từ tháng mấy?");
        } else if (lower.contains("học phí") || lower.contains("thanh toán") || lower.contains("tiền")) {
            list.add("Có khóa học nào được học miễn phí không?");
            list.add("Thanh toán quét mã VietQR bao lâu thì được vào học?");
            list.add("Phụ huynh có thể mua khóa học cho con được không?");
        } else {
            list.add("Website hiện có những khóa học nào tốt nhất?");
            list.add("Tư vấn khóa học phù hợp theo khối lớp");
            list.add("Chính sách hoàn tiền và cấp chứng chỉ số");
        }

        return list;
    }
}

