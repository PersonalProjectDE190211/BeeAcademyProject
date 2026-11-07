package com.beeacademy.backend.infrastructure.family.persistence.adapter;

import com.beeacademy.backend.domain.family.ParentLinkStatus;
import com.beeacademy.backend.domain.family.ParentStudentLink;
import com.beeacademy.backend.domain.family.ParentStudentLinkRepositoryPort;
import com.beeacademy.backend.infrastructure.family.persistence.entity.ParentStudentLinkJpaEntity;
import com.beeacademy.backend.infrastructure.family.persistence.repository.SpringDataParentStudentLinkRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ParentStudentLinkRepositoryAdapter implements ParentStudentLinkRepositoryPort {

    private final SpringDataParentStudentLinkRepository linkRepository;

    public ParentStudentLinkRepositoryAdapter(SpringDataParentStudentLinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Override
    @Transactional
    public ParentStudentLink save(ParentStudentLink link) {
        ParentStudentLinkJpaEntity entity = new ParentStudentLinkJpaEntity(
                link.getId(),
                link.getParentId(),
                link.getStudentId(),
                link.getStudentEmail(),
                link.getInviteToken(),
                link.getStatus().name().toLowerCase(),
                link.isSensitiveDataConsentGranted(),
                link.getInviteExpiresAt(),
                link.getLinkedAt(),
                link.getUnlinkedAt(),
                link.getUnlinkedBy(),
                link.getUnlinkReason(),
                link.getCreatedAt(),
                link.getUpdatedAt()
        );

        ParentStudentLinkJpaEntity saved = linkRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ParentStudentLink> findById(UUID id) {
        return linkRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ParentStudentLink> findByInviteToken(String token) {
        return linkRepository.findByInviteToken(token).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ParentStudentLink> findByParentIdAndStudentId(UUID parentId, UUID studentId) {
        return linkRepository.findByParentIdAndStudentId(parentId, studentId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentStudentLink> findByParentId(UUID parentId) {
        return linkRepository.findByParentIdOrderByCreatedAtDesc(parentId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentStudentLink> findActiveLinksByParentId(UUID parentId) {
        return linkRepository.findByParentIdAndStatusOrderByCreatedAtDesc(parentId, "active").stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParentStudentLink> findByStudentId(UUID studentId) {
        return linkRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveLink(UUID parentId, UUID studentId) {
        return linkRepository.existsByParentIdAndStudentIdAndStatus(parentId, studentId, "active");
    }

    private ParentStudentLink toDomain(ParentStudentLinkJpaEntity entity) {
        ParentLinkStatus status = ParentLinkStatus.valueOf(entity.getStatus().toUpperCase());

        return new ParentStudentLink(
                entity.getId(),
                entity.getParentId(),
                entity.getStudentId(),
                entity.getStudentEmail(),
                entity.getInviteToken(),
                status,
                entity.isConsentGranted(),
                entity.getInviteExpiresAt(),
                entity.getLinkedAt(),
                entity.getUnlinkedAt(),
                entity.getUnlinkedBy(),
                entity.getUnlinkReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                Collections.emptyList()
        );
    }
}

