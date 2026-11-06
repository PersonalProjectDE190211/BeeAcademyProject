package com.beeacademy.backend.infrastructure.course.persistence.adapter;

import com.beeacademy.backend.domain.common.CourseSlug;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.course.*;
import com.beeacademy.backend.infrastructure.course.persistence.entity.ChapterJpaEntity;
import com.beeacademy.backend.infrastructure.course.persistence.entity.CourseJpaEntity;
import com.beeacademy.backend.infrastructure.course.persistence.entity.LessonJpaEntity;
import com.beeacademy.backend.infrastructure.course.persistence.repository.SpringDataCourseRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter triển khai Domain Port {@link CourseRepositoryPort} bằng Spring Data JPA.
 */
@Component
public class CourseRepositoryAdapter implements CourseRepositoryPort {

    private final SpringDataCourseRepository repository;

    public CourseRepositoryAdapter(SpringDataCourseRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Course save(Course course) {
        CourseJpaEntity entity = toEntity(course);
        CourseJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findById(UUID id) {
        return repository.findByIdWithDetails(id).or(() -> repository.findById(id)).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Course> findBySlug(String slug) {
        return repository.findBySlugWithDetails(slug).or(() -> repository.findBySlug(slug)).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findByTeacherId(UUID teacherId) {
        return repository.findByTeacherIdOrderByCreatedAtDesc(teacherId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findPublishedCourses(UUID categoryId, Integer grade) {
        return repository.findPublishedCourses(categoryId).stream()
                .map(this::toDomain)
                .filter(c -> grade == null || c.getGrades().isEmpty() || c.getGrades().contains(grade))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findByStatus(CourseStatus status) {
        if (status == null) return List.of();
        return repository.findByStatusOrderByCreatedAtDesc(status.name().toLowerCase()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private Course toDomain(CourseJpaEntity entity) {
        List<Chapter> chapters = new ArrayList<>();
        if (entity.getChapters() != null) {
            for (ChapterJpaEntity chEntity : entity.getChapters()) {
                List<Lesson> lessons = new ArrayList<>();
                if (chEntity.getLessons() != null) {
                    for (LessonJpaEntity lesEntity : chEntity.getLessons()) {
                        lessons.add(new Lesson(
                                lesEntity.getId(),
                                chEntity.getId(),
                                lesEntity.getTitle(),
                                lesEntity.getPosition(),
                                lesEntity.getVideoStoragePath(),
                                lesEntity.getDurationSec(),
                                lesEntity.isFreePreview(),
                                new ArrayList<>()
                        ));
                    }
                }
                chapters.add(new Chapter(
                        chEntity.getId(),
                        entity.getId(),
                        chEntity.getTitle(),
                        chEntity.getDescription(),
                        chEntity.getPosition(),
                        lessons
                ));
            }
        }

        CourseStatus status = CourseStatus.valueOf(entity.getStatus().toUpperCase());
        MoneyVnd price = MoneyVnd.of(entity.getPriceVnd());

        return new Course(
                entity.getId(),
                entity.getTeacherId(),
                entity.getCategoryId(),
                entity.getTitle(),
                CourseSlug.of(entity.getSlug()),
                entity.getDescription(),
                entity.getThumbnailUrl(),
                price,
                status,
                new ArrayList<>(), // grades
                entity.getTotalChapters(),
                entity.getTotalLessons(),
                entity.getTotalDurationSec(),
                entity.getAvgRating(),
                entity.getReviewCount(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                chapters,
                new ArrayList<>()
        );
    }

    private CourseJpaEntity toEntity(Course domain) {
        CourseJpaEntity courseEntity = new CourseJpaEntity(
                domain.getId(),
                domain.getTeacherId(),
                domain.getCategoryId(),
                domain.getTitle(),
                domain.getSlug().value(),
                domain.getDescription(),
                domain.getThumbnailUrl(),
                domain.getPrice().amount(),
                null,
                domain.getStatus().name().toLowerCase(),
                domain.getTotalChapters(),
                domain.getTotalLessons(),
                domain.getTotalDurationSec(),
                domain.getAverageRating(),
                domain.getTotalReviews(),
                domain.getPublishedAt(),
                domain.getPublishedAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );

        List<ChapterJpaEntity> chapterEntities = new ArrayList<>();
        if (domain.getChapters() != null) {
            for (Chapter chapter : domain.getChapters()) {
                ChapterJpaEntity chapterEntity = new ChapterJpaEntity(
                        chapter.getId(),
                        courseEntity,
                        chapter.getTitle(),
                        chapter.getDescription(),
                        chapter.getOrderIndex(),
                        Instant.now(),
                        Instant.now()
                );

                List<LessonJpaEntity> lessonEntities = new ArrayList<>();
                if (chapter.getLessons() != null) {
                    for (Lesson lesson : chapter.getLessons()) {
                        lessonEntities.add(new LessonJpaEntity(
                                lesson.getId(),
                                chapterEntity,
                                lesson.getTitle(),
                                "",
                                lesson.getOrderIndex(),
                                lesson.isFreePreview(),
                                lesson.getVideoUrl(),
                                lesson.getDurationSec(),
                                "[]",
                                Instant.now(),
                                Instant.now()
                        ));
                    }
                }
                chapterEntity.setLessons(lessonEntities);
                chapterEntities.add(chapterEntity);
            }
        }
        courseEntity.setChapters(chapterEntities);
        return courseEntity;
    }
}
