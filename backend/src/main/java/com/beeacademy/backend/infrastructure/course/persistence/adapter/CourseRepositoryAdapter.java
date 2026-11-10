package com.beeacademy.backend.infrastructure.course.persistence.adapter;

import com.beeacademy.backend.domain.common.CourseSlug;
import com.beeacademy.backend.domain.common.MoneyVnd;
import com.beeacademy.backend.domain.course.*;
import com.beeacademy.backend.infrastructure.course.persistence.entity.ChapterJpaEntity;
import com.beeacademy.backend.infrastructure.course.persistence.entity.CourseJpaEntity;
import com.beeacademy.backend.infrastructure.course.persistence.entity.LessonJpaEntity;
import com.beeacademy.backend.infrastructure.course.persistence.repository.SpringDataCourseRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Adapter triển khai Domain Port {@link CourseRepositoryPort} bằng Spring Data JPA.
 */
@Component
public class CourseRepositoryAdapter implements CourseRepositoryPort {

    private final SpringDataCourseRepository repository;
    private final ObjectMapper objectMapper;

    public CourseRepositoryAdapter(SpringDataCourseRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
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
                                parseDocuments(lesEntity.getDocumentsJson(), lesEntity.getId())
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
        List<Integer> grades = new ArrayList<>();
        if (entity.getGrades() != null) {
            grades.addAll(Arrays.asList(entity.getGrades()));
        }

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
                grades,
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
        Integer[] grades = domain.getGrades() != null
                ? domain.getGrades().toArray(new Integer[0])
                : new Integer[0];

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
                grades,
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
                                serializeDocuments(lesson.getDocuments()),
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

    private List<CourseDocument> parseDocuments(String json, UUID lessonId) {
        if (json == null || json.isBlank() || json.trim().equals("[]")) {
            return new ArrayList<>();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray()) return new ArrayList<>();
            List<CourseDocument> docs = new ArrayList<>();
            for (JsonNode node : root) {
                UUID docId = node.has("id") && !node.get("id").asText().isBlank()
                        ? UUID.fromString(node.get("id").asText())
                        : UUID.randomUUID();
                String title = node.has("title") ? node.get("title").asText()
                        : (node.has("name") ? node.get("name").asText() : "Tài liệu học tập");
                String fileUrl = node.has("fileUrl") ? node.get("fileUrl").asText() : "";
                long size = node.has("fileSizeBytes") ? node.get("fileSizeBytes").asLong() : 0L;
                String typeStr = node.has("fileType") ? node.get("fileType").asText()
                        : (node.has("documentType") ? node.get("documentType").asText() : "PDF");

                DocumentType docType;
                try {
                    docType = DocumentType.valueOf(typeStr.toUpperCase());
                } catch (Exception e) {
                    docType = DocumentType.PDF;
                }

                if (!fileUrl.isBlank()) {
                    docs.add(new CourseDocument(docId, lessonId, title, fileUrl, size, docType));
                }
            }
            return docs;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String serializeDocuments(List<CourseDocument> docs) {
        if (docs == null || docs.isEmpty()) {
            return "[]";
        }
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            for (CourseDocument doc : docs) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", doc.getId().toString());
                map.put("name", doc.getTitle());
                map.put("title", doc.getTitle());
                map.put("fileUrl", doc.getFileUrl());
                map.put("fileType", doc.getDocumentType() != null ? doc.getDocumentType().name().toLowerCase() : "pdf");
                map.put("fileSizeBytes", doc.getFileSizeBytes());
                list.add(map);
            }
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}
