package com.beeacademy.backend.infrastructure.learning.persistence.repository;

import com.beeacademy.backend.infrastructure.learning.persistence.entity.LessonDiscussionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataLessonDiscussionRepository extends JpaRepository<LessonDiscussionJpaEntity, UUID> {

    List<LessonDiscussionJpaEntity> findByLessonIdAndParentIdIsNullOrderByCreatedAtAsc(UUID lessonId);

    List<LessonDiscussionJpaEntity> findByParentIdOrderByCreatedAtAsc(UUID parentId);

    List<LessonDiscussionJpaEntity> findByLessonIdOrderByCreatedAtAsc(UUID lessonId);
}
