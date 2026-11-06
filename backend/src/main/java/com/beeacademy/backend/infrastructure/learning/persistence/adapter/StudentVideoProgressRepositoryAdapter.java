package com.beeacademy.backend.infrastructure.learning.persistence.adapter;

import com.beeacademy.backend.domain.learning.StudentVideoProgress;
import com.beeacademy.backend.domain.learning.StudentVideoProgressRepositoryPort;
import com.beeacademy.backend.domain.learning.WatchSegment;
import com.beeacademy.backend.domain.learning.WatchSegments;
import com.beeacademy.backend.infrastructure.learning.persistence.entity.LessonProgressJpaEntity;
import com.beeacademy.backend.infrastructure.learning.persistence.repository.SpringDataLessonProgressRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter triển khai Domain Port {@link StudentVideoProgressRepositoryPort} bằng Spring Data JPA.
 */
@Component
public class StudentVideoProgressRepositoryAdapter implements StudentVideoProgressRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(StudentVideoProgressRepositoryAdapter.class);

    private final SpringDataLessonProgressRepository repository;
    private final ObjectMapper objectMapper;

    public StudentVideoProgressRepositoryAdapter(SpringDataLessonProgressRepository repository,
                                                ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public StudentVideoProgress save(StudentVideoProgress progress) {
        LessonProgressJpaEntity entity = toEntity(progress);
        LessonProgressJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<StudentVideoProgress> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<StudentVideoProgress> findByStudentIdAndLessonId(UUID studentId, UUID lessonId) {
        return repository.findByStudentIdAndLessonId(studentId, lessonId).map(this::toDomain);
    }

    @Override
    public List<StudentVideoProgress> findByStudentIdAndCourseId(UUID studentId, UUID courseId) {
        return repository.findByStudentIdAndCourseId(studentId, courseId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countCompletedLessons(UUID studentId, UUID courseId) {
        return repository.countCompletedLessons(studentId, courseId);
    }

    private StudentVideoProgress toDomain(LessonProgressJpaEntity entity) {
        WatchSegments segments = new WatchSegments();
        if (entity.getWatchSegments() != null && !entity.getWatchSegments().isBlank()) {
            try {
                List<WatchSegmentRecord> rawList = objectMapper.readValue(
                        entity.getWatchSegments(),
                        new TypeReference<List<WatchSegmentRecord>>() {}
                );
                for (WatchSegmentRecord r : rawList) {
                    segments.addSegment(r.startSec(), r.endSec());
                }
            } catch (Exception e) {
                log.warn("Lỗi parse watch_segments JSON: {}", e.getMessage());
            }
        }

        return new StudentVideoProgress(
                entity.getId(),
                entity.getStudentId(),
                entity.getCourseId(),
                entity.getLessonId(),
                segments,
                entity.getLastPositionSec(),
                entity.isCompleted(),
                entity.getCompletedAt(),
                entity.getUpdatedAt()
        );
    }

    private LessonProgressJpaEntity toEntity(StudentVideoProgress domain) {
        String segmentsJson = "[]";
        if (domain.getWatchSegments() != null && !domain.getWatchSegments().getSegments().isEmpty()) {
            try {
                List<WatchSegmentRecord> records = domain.getWatchSegments().getSegments().stream()
                        .map(s -> new WatchSegmentRecord(s.startSec(), s.endSec()))
                        .toList();
                segmentsJson = objectMapper.writeValueAsString(records);
            } catch (Exception e) {
                log.warn("Lỗi serialize watch_segments JSON: {}", e.getMessage());
            }
        }

        int watchedSecs = domain.getWatchSegments() != null ? domain.getWatchSegments().calculateTotalUniqueSeconds() : 0;

        return new LessonProgressJpaEntity(
                domain.getId(),
                domain.getStudentId(),
                domain.getCourseId(),
                domain.getLessonId(),
                watchedSecs,
                domain.getLastPositionSec(),
                segmentsJson,
                "[]",
                "[]",
                domain.isCompleted(),
                domain.getCompletedAt(),
                Instant.now()
        );
    }

    private record WatchSegmentRecord(int startSec, int endSec) {}
}
