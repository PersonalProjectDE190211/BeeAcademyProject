package com.beeacademy.backend.application.course.usecase;

import com.beeacademy.backend.application.course.dto.CourseDetailResult;
import com.beeacademy.backend.domain.common.DomainException;
import com.beeacademy.backend.domain.course.Course;
import com.beeacademy.backend.domain.course.CourseRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case Xem chi tiết khóa học và cấu trúc chương/bài giảng (UC07, UC08).
 * Khách vãng lai chỉ xem được video URL của bài học thử (isFreePreview = true).
 */
@Service
public class GetCourseDetailUseCase {

    private final CourseRepositoryPort courseRepository;

    public GetCourseDetailUseCase(CourseRepositoryPort courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public CourseDetailResult getBySlug(String slug) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new DomainException("COURSE_NOT_FOUND", "Không tìm thấy khóa học với slug: " + slug));
        // Khách xem chi tiết công khai: videoUrl chỉ hiển thị cho bài thử
        return CourseDetailResult.from(course, false);
    }

    @Transactional(readOnly = true)
    public CourseDetailResult getById(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new DomainException("COURSE_NOT_FOUND", "Không tìm thấy khóa học với ID: " + id));
        return CourseDetailResult.from(course, false);
    }
}

