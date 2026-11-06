package com.beeacademy.backend.infrastructure.course.persistence.adapter;

import com.beeacademy.backend.domain.course.CourseReview;
import com.beeacademy.backend.domain.course.CourseReviewRepositoryPort;
import com.beeacademy.backend.infrastructure.course.persistence.entity.CourseReviewJpaEntity;
import com.beeacademy.backend.infrastructure.course.persistence.repository.SpringDataCourseReviewRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter triển khai Domain Port {@link CourseReviewRepositoryPort} bằng Spring Data JPA.
 */
@Component
public class CourseReviewRepositoryAdapter implements CourseReviewRepositoryPort {

    private final SpringDataCourseReviewRepository repository;

    public CourseReviewRepositoryAdapter(SpringDataCourseReviewRepository repository) {
        this.repository = repository;
    }

    @Override
    public CourseReview save(CourseReview review) {
        CourseReviewJpaEntity entity = toEntity(review);
        CourseReviewJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<CourseReview> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<CourseReview> findByCourseIdAndStudentId(UUID courseId, UUID studentId) {
        return repository.findByCourseIdAndStudentId(courseId, studentId).map(this::toDomain);
    }

    @Override
    public List<CourseReview> findByCourseId(UUID courseId) {
        return repository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private CourseReview toDomain(CourseReviewJpaEntity entity) {
        return new CourseReview(
                entity.getId(),
                entity.getCourseId(),
                entity.getStudentId(),
                entity.getRating(),
                entity.getComment(),
                false,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private CourseReviewJpaEntity toEntity(CourseReview domain) {
        return new CourseReviewJpaEntity(
                domain.getId(),
                domain.getCourseId(),
                domain.getStudentId(),
                domain.getRating(),
                domain.getComment(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
