package com.beeacademy.backend.infrastructure.learning.persistence.adapter;

import com.beeacademy.backend.domain.learning.QaMessage;
import com.beeacademy.backend.domain.learning.QaThread;
import com.beeacademy.backend.domain.learning.QaThreadRepositoryPort;
import com.beeacademy.backend.infrastructure.learning.persistence.entity.LessonDiscussionJpaEntity;
import com.beeacademy.backend.infrastructure.learning.persistence.repository.SpringDataLessonDiscussionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter triển khai Domain Port {@link QaThreadRepositoryPort} bằng bảng lesson_discussions.
 */
@Component
public class QaThreadRepositoryAdapter implements QaThreadRepositoryPort {

    private final SpringDataLessonDiscussionRepository repository;

    public QaThreadRepositoryAdapter(SpringDataLessonDiscussionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public QaThread save(QaThread thread) {
        // 1. Lưu câu hỏi gốc (parent discussion)
        LessonDiscussionJpaEntity parent = new LessonDiscussionJpaEntity(
                thread.getId(),
                thread.getLessonId(),
                thread.getCourseId(),
                thread.getStudentId(),
                "STUDENT",
                null,
                thread.getContent(),
                thread.isResolved(),
                thread.getCreatedAt(),
                thread.getUpdatedAt()
        );
        repository.save(parent);

        // 2. Lưu các tin nhắn phản hồi con (child replies)
        if (thread.getMessages() != null) {
            for (QaMessage msg : thread.getMessages()) {
                LessonDiscussionJpaEntity reply = new LessonDiscussionJpaEntity(
                        msg.getId(),
                        thread.getLessonId(),
                        thread.getCourseId(),
                        msg.getSenderId(),
                        "USER",
                        thread.getId(),
                        msg.getContent(),
                        false,
                        msg.getCreatedAt(),
                        msg.getCreatedAt()
                );
                repository.save(reply);
            }
        }

        return findById(thread.getId()).orElse(thread);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QaThread> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QaThread> findByLessonId(UUID lessonId) {
        return repository.findByLessonIdAndParentIdIsNullOrderByCreatedAtAsc(lessonId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QaThread> findByCourseId(UUID courseId) {
        return repository.findAll().stream()
                .filter(d -> courseId.equals(d.getCourseId()) && d.getParentId() == null)
                .map(this::toDomain)
                .toList();
    }

    private QaThread toDomain(LessonDiscussionJpaEntity entity) {
        List<QaMessage> messages = new ArrayList<>();
        List<LessonDiscussionJpaEntity> replies = repository.findByParentIdOrderByCreatedAtAsc(entity.getId());
        for (LessonDiscussionJpaEntity r : replies) {
            messages.add(new QaMessage(
                    r.getId(),
                    entity.getId(),
                    r.getAuthorId(),
                    r.getContent(),
                    r.getCreatedAt()
            ));
        }

        return new QaThread(
                entity.getId(),
                entity.getCourseId(),
                entity.getLessonId(),
                entity.getAuthorId(),
                "Thảo luận bài học", // title fallback
                entity.getContent(),
                entity.isResolved(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                messages
        );
    }
}
