package com.beeacademy.backend.api.course;

import com.beeacademy.backend.api.common.ApiResponse;
import com.beeacademy.backend.application.course.dto.CategoryResult;
import com.beeacademy.backend.application.course.dto.CourseDetailResult;
import com.beeacademy.backend.application.course.dto.CourseReviewResult;
import com.beeacademy.backend.application.course.dto.CourseSummaryResult;
import com.beeacademy.backend.application.course.usecase.BrowseCatalogUseCase;
import com.beeacademy.backend.application.course.usecase.GetCourseDetailUseCase;
import com.beeacademy.backend.application.course.usecase.ListCategoriesUseCase;
import com.beeacademy.backend.application.course.usecase.ManageCourseReviewUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller công khai tra cứu Khóa học và Danh mục môn học (UC06, UC07, UC08).
 * Không yêu cầu đăng nhập.
 */
@Tag(name = "3. Khóa học công khai (Public Catalog)", description = "Các API tìm kiếm, lọc danh mục và xem chi tiết khóa học")
@RestController
@RequestMapping("/api/courses/public")
public class PublicCourseController {

    private final BrowseCatalogUseCase browseCatalogUseCase;
    private final GetCourseDetailUseCase getCourseDetailUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final ManageCourseReviewUseCase manageCourseReviewUseCase;

    public PublicCourseController(BrowseCatalogUseCase browseCatalogUseCase,
                                  GetCourseDetailUseCase getCourseDetailUseCase,
                                  ListCategoriesUseCase listCategoriesUseCase,
                                  ManageCourseReviewUseCase manageCourseReviewUseCase) {
        this.browseCatalogUseCase = browseCatalogUseCase;
        this.getCourseDetailUseCase = getCourseDetailUseCase;
        this.listCategoriesUseCase = listCategoriesUseCase;
        this.manageCourseReviewUseCase = manageCourseReviewUseCase;
    }

    /**
     * UC06: Tìm kiếm và lọc danh mục khóa học đã xuất bản.
     */
    @Operation(summary = "Tìm kiếm & Lọc khóa học", description = "Lọc khóa học theo từ khóa, danh mục, khối lớp, giá và sắp xếp")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseSummaryResult>>> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false, defaultValue = "newest") String sortBy
    ) {
        List<CourseSummaryResult> results = browseCatalogUseCase.search(
                keyword, categoryId, grade, minPrice, maxPrice, sortBy
        );
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * UC07, UC08: Xem chi tiết khóa học theo đường dẫn Slug.
     */
    @Operation(summary = "Xem chi tiết khóa học theo Slug", description = "Bao gồm cây chương và bài giảng (chỉ bài học thử mới hiển thị video URL)")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CourseDetailResult>> getCourseBySlug(@PathVariable String slug) {
        CourseDetailResult detail = getCourseDetailUseCase.getBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * UC07, UC08: Xem chi tiết khóa học theo ID.
     */
    @Operation(summary = "Xem chi tiết khóa học theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailResult>> getCourseById(@PathVariable UUID id) {
        CourseDetailResult detail = getCourseDetailUseCase.getById(id);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * Lấy danh mục các môn học đang hoạt động.
     */
    @Operation(summary = "Danh mục môn học", description = "Danh sách môn học đang hoạt động để hiển thị menu hoặc bộ lọc")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResult>>> getCategories() {
        List<CategoryResult> categories = listCategoriesUseCase.execute();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * Xem danh sách đánh giá nhận xét của khóa học.
     */
    @Operation(summary = "Đánh giá của khóa học", description = "Danh sách đánh giá sao và bình luận của học viên")
    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<List<CourseReviewResult>>> getCourseReviews(@PathVariable UUID id) {
        List<CourseReviewResult> reviews = manageCourseReviewUseCase.getCourseReviews(id);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
}

