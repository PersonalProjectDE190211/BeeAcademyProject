package com.beeacademy.backend.application.ai.service;

import com.beeacademy.backend.api.ai.dto.ChatbotCourseCardDto;
import com.beeacademy.backend.domain.ai.CourseSearchFilter;
import com.beeacademy.backend.domain.ai.SubjectCode;
import com.beeacademy.backend.domain.course.Category;
import com.beeacademy.backend.domain.course.CategoryRepositoryPort;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.CourseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service tìm kiếm và hậu kiểm (Post-Validation) khóa học phục vụ Trợ lý BeeBot (Giai đoạn 2 & 4).
 */
@Service
public class CourseSearchService {

    private static final Logger log = LoggerFactory.getLogger(CourseSearchService.class);

    private final CourseRepositoryPort courseRepository;
    private final CategoryRepositoryPort categoryRepository;

    public CourseSearchService(CourseRepositoryPort courseRepository, CategoryRepositoryPort categoryRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Giai đoạn 2 & 4: Tìm kiếm khóa học theo bộ lọc và thực hiện kiểm tra tính toàn vẹn (Post-Validation).
     */
    public List<ChatbotCourseCardDto> searchAndVerify(CourseSearchFilter filter) {
        if (filter == null) {
            filter = CourseSearchFilter.empty();
        }

        List<Category> allCategories = categoryRepository.findAllActive();
        Map<UUID, Category> categoryMap = new HashMap<>();
        for (Category cat : allCategories) {
            categoryMap.put(cat.getId(), cat);
        }

        // 1. Xác định Category ID (nếu filter có môn học rõ ràng)
        UUID targetCategoryId = null;
        if (filter.subject() != null) {
            String slug = filter.subject().getCategorySlug();
            for (Category cat : allCategories) {
                if (cat.getSlug().equalsIgnoreCase(slug)) {
                    targetCategoryId = cat.getId();
                    break;
                }
            }
        }

        // 2. Truy vấn từ Database (chỉ lấy published)
        List<Course> rawCourses = courseRepository.findPublishedCourses(targetCategoryId, filter.grade());

        // 3. Giai đoạn 4: Hậu kiểm (Post-Validation) chặt chẽ
        List<ChatbotCourseCardDto> verifiedCards = new ArrayList<>();
        for (Course course : rawCourses) {
            if (!isValidForFilter(course, filter, categoryMap)) {
                continue;
            }

            Category cat = categoryMap.get(course.getCategoryId());
            String subjectCodeName = filter.subject() != null
                    ? filter.subject().name()
                    : detectSubjectFromCourse(course, cat);
            String subjectDisplayName = filter.subject() != null
                    ? filter.subject().getDisplayName()
                    : (cat != null ? cat.getName() : "Khóa học K-12");

            Integer matchedGrade = (filter.grade() != null && course.getGrades().contains(filter.grade()))
                    ? filter.grade()
                    : (course.getGrades().isEmpty() ? null : course.getGrades().get(0));

            int price = course.getPrice() != null ? course.getPrice().amount() : 0;
            double rating = course.getAverageRating() != null ? course.getAverageRating().doubleValue() : 5.0;

            ChatbotCourseCardDto card = new ChatbotCourseCardDto(
                    course.getId(),
                    course.getTitle(),
                    subjectCodeName,
                    subjectDisplayName,
                    matchedGrade,
                    price,
                    null,
                    rating,
                    course.getTotalReviews(),
                    course.getTotalLessons(),
                    course.getThumbnailUrl(),
                    "/courses/" + course.getId()
            );

            verifiedCards.add(card);
        }

        // Sắp xếp nếu có sortBy
        if ("PRICE_ASC".equalsIgnoreCase(filter.sortBy())) {
            verifiedCards.sort(Comparator.comparingInt(ChatbotCourseCardDto::price));
        } else if ("PRICE_DESC".equalsIgnoreCase(filter.sortBy())) {
            verifiedCards.sort(Comparator.comparingInt(ChatbotCourseCardDto::price).reversed());
        }

        log.info("CourseSearchService: filter={}, rawCount={}, verifiedCount={}", filter, rawCourses.size(), verifiedCards.size());
        return verifiedCards;
    }

    private boolean isValidForFilter(Course course, CourseSearchFilter filter, Map<UUID, Category> categoryMap) {
        if (course == null || course.getId() == null) {
            return false;
        }

        // 1. Phải ở trạng thái PUBLISHED
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            return false;
        }

        // 2. Kiểm tra khối lớp (nếu người dùng yêu cầu cụ thể)
        if (filter.grade() != null) {
            if (course.getGrades() == null || !course.getGrades().contains(filter.grade())) {
                return false;
            }
        }

        // 3. Kiểm tra môn học (nếu người dùng yêu cầu cụ thể)
        if (filter.subject() != null) {
            Category cat = categoryMap.get(course.getCategoryId());
            String titleLower = course.getTitle() != null ? course.getTitle().toLowerCase() : "";
            String slugLower = course.getSlug() != null ? course.getSlug().value().toLowerCase() : "";

            SubjectCode targetSubject = filter.subject();
            // Đối với trường hợp chung category (VD Vật lý vs Hóa học trong khoa-hoc-tu-nhien)
            if (targetSubject == SubjectCode.PHYSICS) {
                if (!titleLower.contains("vật lý") && !titleLower.contains("vật lí") && !slugLower.contains("vat-ly") && !titleLower.contains("dao động")) {
                    return false;
                }
            } else if (targetSubject == SubjectCode.CHEMISTRY) {
                if (!titleLower.contains("hóa") && !slugLower.contains("hoa-hoc") && !titleLower.contains("este")) {
                    return false;
                }
            } else if (targetSubject == SubjectCode.BIOLOGY) {
                if (!titleLower.contains("sinh") && !slugLower.contains("sinh-hoc")) {
                    return false;
                }
            } else {
                // Kiểm tra theo category slug
                if (cat != null && !cat.getSlug().equalsIgnoreCase(targetSubject.getCategorySlug())) {
                    return false;
                }
            }
        }

        // 4. Kiểm tra ngân sách (nếu có)
        if (filter.maxBudget() != null && filter.maxBudget() > 0) {
            int price = course.getPrice() != null ? course.getPrice().amount() : 0;
            if (price > filter.maxBudget()) {
                return false;
            }
        }

        return true;
    }

    private String detectSubjectFromCourse(Course course, Category category) {
        String titleLower = course.getTitle() != null ? course.getTitle().toLowerCase() : "";
        if (titleLower.contains("toán")) return SubjectCode.MATH.name();
        if (titleLower.contains("vật lý") || titleLower.contains("vật lí")) return SubjectCode.PHYSICS.name();
        if (titleLower.contains("hóa")) return SubjectCode.CHEMISTRY.name();
        if (titleLower.contains("tiếng anh")) return SubjectCode.ENGLISH.name();
        if (titleLower.contains("tin học") || titleLower.contains("lập trình")) return SubjectCode.IT.name();
        if (titleLower.contains("văn")) return SubjectCode.LITERATURE.name();
        return category != null ? category.getSlug() : "OTHER";
    }

    /**
     * Định dạng danh sách khóa học thực tế thành dữ liệu mô tả cho Gemini sinh lời tư vấn (Giai đoạn 3).
     */
    public String buildCourseKnowledgeSummary(List<ChatbotCourseCardDto> courses, CourseSearchFilter filter) {
        if (courses == null || courses.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("KẾT QUẢ TÌM KIẾM TỪ DATABASE: KHÔNG CÓ KHÓA HỌC PHÙ HỢP.\n");
            if (filter.subject() != null) {
                sb.append("- Môn học yêu cầu: ").append(filter.subject().getDisplayName()).append("\n");
            }
            if (filter.grade() != null) {
                sb.append("- Khối lớp yêu cầu: Lớp ").append(filter.grade()).append("\n");
            }
            sb.append("QUY TẮC BẮT BUỘC:\n");
            sb.append("1. Bạn phải thông báo rõ ràng là hiện tại BeeAcademy chưa có khóa học cho yêu cầu này.\n");
            sb.append("2. TUYỆT ĐỐI KHÔNG tự ý giới thiệu một môn học khác (như Tiếng Anh hay môn khác) để thay thế môn người dùng yêu cầu.\n");
            sb.append("3. Gợi ý người dùng tham khảo các khối lớp lân cận nếu có hoặc chờ chương trình mới.");
            return sb.toString();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("KẾT QUẢ TÌM KIẾM TỪ DATABASE: TÌM THẤY ").append(courses.size()).append(" KHÓA HỌC PHÙ HỢP:\n");
        for (int i = 0; i < courses.size(); i++) {
            ChatbotCourseCardDto c = courses.get(i);
            String priceStr = c.price() == 0 ? "MIỄN PHÍ (0 VNĐ)" : String.format("%,d VNĐ", c.price());
            sb.append(String.format("%d. Khóa học: %s\n   - Môn học: %s | Khối lớp: %s\n   - Học phí: %s\n   - Đánh giá: %.1f/5.0 (%d đánh giá) | Đề cương: %d bài học\n",
                    i + 1,
                    c.title(),
                    c.subjectLabel(),
                    c.grade() != null ? "Lớp " + c.grade() : "K-12",
                    priceStr,
                    c.rating(),
                    c.reviewCount(),
                    c.totalLessons()
            ));
        }
        sb.append("\nQUY TẮC BẮT BUỘC:\n");
        sb.append("1. Giới thiệu ngắn gọn, lịch sự về các khóa học trên.\n");
        sb.append("2. Không cần chèn link URL thủ công hoặc tự viết lại giá tiền vì hệ thống sẽ tự render Course Card bên dưới câu trả lời của bạn.");
        return sb.toString();
    }
}

