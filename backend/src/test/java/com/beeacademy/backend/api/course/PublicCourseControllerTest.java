package com.beeacademy.backend.api.course;

import com.beeacademy.backend.api.common.GlobalExceptionHandler;
import com.beeacademy.backend.application.course.dto.CategoryResult;
import com.beeacademy.backend.application.course.dto.CourseDetailResult;
import com.beeacademy.backend.application.course.dto.CourseSummaryResult;
import com.beeacademy.backend.application.course.usecase.BrowseCatalogUseCase;
import com.beeacademy.backend.application.course.usecase.GetCourseDetailUseCase;
import com.beeacademy.backend.application.course.usecase.ListCategoriesUseCase;
import com.beeacademy.backend.application.course.usecase.ManageCourseReviewUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("PublicCourseController MockMvc Tests")
class PublicCourseControllerTest {

    private MockMvc mockMvc;
    private BrowseCatalogUseCase browseCatalogUseCase;
    private GetCourseDetailUseCase getCourseDetailUseCase;
    private ListCategoriesUseCase listCategoriesUseCase;
    private ManageCourseReviewUseCase manageCourseReviewUseCase;

    @BeforeEach
    void setUp() {
        browseCatalogUseCase = Mockito.mock(BrowseCatalogUseCase.class);
        getCourseDetailUseCase = Mockito.mock(GetCourseDetailUseCase.class);
        listCategoriesUseCase = Mockito.mock(ListCategoriesUseCase.class);
        manageCourseReviewUseCase = Mockito.mock(ManageCourseReviewUseCase.class);

        PublicCourseController controller = new PublicCourseController(
                browseCatalogUseCase, getCourseDetailUseCase, listCategoriesUseCase, manageCourseReviewUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/courses/public -> Trả về danh sách khóa học")
    void searchCourses_Success() throws Exception {
        CourseSummaryResult item = new CourseSummaryResult(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Toán 8 Cơ Bản", "toan-8-co-ban", "Mô tả toán", "https://thumb.jpg",
                100000, "PUBLISHED", 1, 3, 1800, BigDecimal.valueOf(5.0), 1,
                Instant.now(), Instant.now()
        );

        when(browseCatalogUseCase.search(any(), any(), any(), any(), any(), any())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/courses/public")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Toán 8 Cơ Bản"))
                .andExpect(jsonPath("$.data[0].slug").value("toan-8-co-ban"));
    }

    @Test
    @DisplayName("GET /api/courses/public/slug/{slug} -> Trả về chi tiết khóa học")
    void getCourseBySlug_Success() throws Exception {
        CourseDetailResult detail = new CourseDetailResult(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Hóa Học 10", "hoa-hoc-10", "Mô tả hóa", null,
                150000, "PUBLISHED", List.of(10), 0, 0, 0,
                BigDecimal.valueOf(4.5), 10, Instant.now(), Instant.now(), Instant.now(),
                List.of()
        );

        when(getCourseDetailUseCase.getBySlug("hoa-hoc-10")).thenReturn(detail);

        mockMvc.perform(get("/api/courses/public/slug/hoa-hoc-10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Hóa Học 10"));
    }

    @Test
    @DisplayName("GET /api/courses/public/categories -> Trả về danh mục môn học")
    void getCategories_Success() throws Exception {
        CategoryResult cat = new CategoryResult(
                UUID.randomUUID(), "Toán Học", "toan-hoc", "Các môn toán", "math_icon", 1, true
        );

        when(listCategoriesUseCase.execute()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/courses/public/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Toán Học"));
    }
}
