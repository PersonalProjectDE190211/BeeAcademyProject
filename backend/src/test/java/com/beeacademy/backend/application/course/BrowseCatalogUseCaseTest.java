package com.beeacademy.backend.application.course;

import com.beeacademy.backend.application.course.dto.CourseSummaryResult;
import com.beeacademy.backend.application.course.usecase.BrowseCatalogUseCase;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import com.beeacademy.backend.domain.course.CourseStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("BrowseCatalogUseCase Unit Tests (UC06)")
class BrowseCatalogUseCaseTest {

    private CourseRepositoryPort courseRepository;
    private BrowseCatalogUseCase useCase;

    @BeforeEach
    void setUp() {
        courseRepository = Mockito.mock(CourseRepositoryPort.class);
        useCase = new BrowseCatalogUseCase(courseRepository);
    }

    @Test
    @DisplayName("Tìm kiếm với từ khóa và khoảng giá thành công")
    void searchWithFilters_Success() {
        UUID categoryId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        Course c1 = new Course(
                UUID.randomUUID(), teacherId, categoryId, "Toán Lớp 8 Nâng Cao",
                null, "Đại số và Hình học lớp 8", null,
                MoneyVnd.of(150000), CourseStatus.PUBLISHED, List.of(8),
                2, 5, 3600, BigDecimal.valueOf(4.8), 10,
                Instant.now(), Instant.now().minusSeconds(100), Instant.now(),
                new ArrayList<>(), new ArrayList<>()
        );

        Course c2 = new Course(
                UUID.randomUUID(), teacherId, categoryId, "Toán Lớp 9 Luyện Thi Vào 10",
                null, "Chuyên đề tuyển sinh vào 10", null,
                MoneyVnd.of(300000), CourseStatus.PUBLISHED, List.of(9),
                4, 10, 7200, BigDecimal.valueOf(4.5), 20,
                Instant.now(), Instant.now(), Instant.now(),
                new ArrayList<>(), new ArrayList<>()
        );

        when(courseRepository.findPublishedCourses(any(), any())).thenReturn(List.of(c1, c2));

        // Test filter by keyword "Lớp 8"
        List<CourseSummaryResult> resultsKeyword = useCase.search("Lớp 8", null, null, null, null, null);
        assertThat(resultsKeyword).hasSize(1);
        assertThat(resultsKeyword.get(0).title()).isEqualTo("Toán Lớp 8 Nâng Cao");

        // Test filter by minPrice = 200000
        List<CourseSummaryResult> resultsPrice = useCase.search(null, null, null, 200000, null, null);
        assertThat(resultsPrice).hasSize(1);
        assertThat(resultsPrice.get(0).title()).isEqualTo("Toán Lớp 9 Luyện Thi Vào 10");

        // Test sorting by price_asc
        List<CourseSummaryResult> sortedAsc = useCase.search(null, null, null, null, null, "price_asc");
        assertThat(sortedAsc).hasSize(2);
        assertThat(sortedAsc.get(0).priceVnd()).isEqualTo(150000);
        assertThat(sortedAsc.get(1).priceVnd()).isEqualTo(300000);

        // Test sorting by rating
        List<CourseSummaryResult> sortedRating = useCase.search(null, null, null, null, null, "rating");
        assertThat(sortedRating).hasSize(2);
        assertThat(sortedRating.get(0).averageRating()).isEqualTo(BigDecimal.valueOf(4.8));
    }
}
