package com.beeacademy.backend.infrastructure.family.persistence.repository;

import com.beeacademy.backend.infrastructure.family.persistence.entity.ParentStudentLinkJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataParentStudentLinkRepository extends JpaRepository<ParentStudentLinkJpaEntity, UUID> {

    Optional<ParentStudentLinkJpaEntity> findByInviteToken(String token);

    Optional<ParentStudentLinkJpaEntity> findByParentIdAndStudentId(UUID parentId, UUID studentId);

    List<ParentStudentLinkJpaEntity> findByParentIdOrderByCreatedAtDesc(UUID parentId);

    List<ParentStudentLinkJpaEntity> findByParentIdAndStatusOrderByCreatedAtDesc(UUID parentId, String status);

    List<ParentStudentLinkJpaEntity> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    boolean existsByParentIdAndStudentIdAndStatus(UUID parentId, UUID studentId, String status);
}

