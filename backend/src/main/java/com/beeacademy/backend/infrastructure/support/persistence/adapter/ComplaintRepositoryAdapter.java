package com.beeacademy.backend.infrastructure.support.persistence.adapter;

import com.beeacademy.backend.domain.support.Complaint;
import com.beeacademy.backend.domain.support.ComplaintAttachment;
import com.beeacademy.backend.domain.support.ComplaintCategory;
import com.beeacademy.backend.domain.support.ComplaintMessage;
import com.beeacademy.backend.domain.support.ComplaintPriority;
import com.beeacademy.backend.domain.support.ComplaintRepositoryPort;
import com.beeacademy.backend.domain.support.ComplaintStatus;
import com.beeacademy.backend.infrastructure.support.persistence.entity.ComplaintJpaEntity;
import com.beeacademy.backend.infrastructure.support.persistence.entity.ComplaintMessageJpaEntity;
import com.beeacademy.backend.infrastructure.support.persistence.repository.SpringDataComplaintMessageRepository;
import com.beeacademy.backend.infrastructure.support.persistence.repository.SpringDataComplaintRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ComplaintRepositoryAdapter implements ComplaintRepositoryPort {

    private final SpringDataComplaintRepository complaintRepository;
    private final SpringDataComplaintMessageRepository messageRepository;

    public ComplaintRepositoryAdapter(
            SpringDataComplaintRepository complaintRepository,
            SpringDataComplaintMessageRepository messageRepository
    ) {
        this.complaintRepository = complaintRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public Complaint save(Complaint complaint) {
        ComplaintJpaEntity entity = new ComplaintJpaEntity(
                complaint.getId(),
                complaint.getSenderId(),
                complaint.getSenderRole(),
                complaint.getTitle(),
                complaint.getCategory().name().toLowerCase(),
                complaint.getPriority().name().toLowerCase(),
                complaint.getStatus().name().toLowerCase(),
                complaint.getResolvedByAdminId(),
                complaint.getResolutionSummary(),
                complaint.getResolvedAt(),
                complaint.getLastActivityAt(),
                complaint.getCreatedAt(),
                complaint.getLastActivityAt()
        );

        ComplaintJpaEntity saved = complaintRepository.save(entity);

        List<ComplaintMessageJpaEntity> messageEntities = new ArrayList<>();
        for (ComplaintMessage msg : complaint.getMessages()) {
            if (!messageRepository.existsById(msg.getId())) {
                messageEntities.add(new ComplaintMessageJpaEntity(
                        msg.getId(),
                        saved,
                        msg.getAuthorId(),
                        msg.getAuthorRole(),
                        msg.getContent(),
                        "[]",
                        false,
                        null,
                        msg.getCreatedAt()
                ));
            }
        }
        if (!messageEntities.isEmpty()) {
            messageRepository.saveAll(messageEntities);
        }

        return loadWithMessages(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Complaint> findById(UUID id) {
        return complaintRepository.findById(id).map(this::loadWithMessages);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> findBySenderId(UUID senderId) {
        return complaintRepository.findBySenderIdOrderByLastActivityAtDesc(senderId).stream()
                .map(this::loadWithMessages)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> findByStatus(ComplaintStatus status) {
        return complaintRepository.findByStatusOrderByLastActivityAtDesc(status.name().toLowerCase()).stream()
                .map(this::loadWithMessages)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> findByCategory(ComplaintCategory category) {
        return complaintRepository.findByCategoryOrderByLastActivityAtDesc(category.name().toLowerCase()).stream()
                .map(this::loadWithMessages)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Complaint> findAllOrderByLastActivityDesc() {
        return complaintRepository.findAllByOrderByLastActivityAtDesc().stream()
                .map(this::loadWithMessages)
                .collect(Collectors.toList());
    }

    private Complaint loadWithMessages(ComplaintJpaEntity entity) {
        List<ComplaintMessageJpaEntity> msgEntities = messageRepository.findByComplaintIdOrderByCreatedAtAsc(entity.getId());
        List<ComplaintMessage> messages = msgEntities.stream()
                .map(m -> new ComplaintMessage(
                        m.getId(),
                        entity.getId(),
                        m.getAuthorId(),
                        m.getAuthorRole(),
                        m.getContent(),
                        m.getCreatedAt()
                ))
                .collect(Collectors.toList());

        ComplaintCategory category;
        try {
            category = ComplaintCategory.valueOf(entity.getCategory().toUpperCase());
        } catch (Exception e) {
            category = ComplaintCategory.OTHER;
        }

        ComplaintPriority priority;
        try {
            priority = ComplaintPriority.valueOf(entity.getPriority().toUpperCase());
        } catch (Exception e) {
            priority = ComplaintPriority.MEDIUM;
        }

        ComplaintStatus status;
        try {
            status = ComplaintStatus.valueOf(entity.getStatus().toUpperCase());
        } catch (Exception e) {
            status = ComplaintStatus.OPEN;
        }

        return new Complaint(
                entity.getId(),
                entity.getSenderId(),
                entity.getSenderRole(),
                entity.getTitle(),
                category,
                priority,
                status,
                entity.getCreatedAt(),
                entity.getLastActivityAt(),
                entity.getResolvedAt(),
                entity.getResolvedByAdmin(),
                entity.getResolutionSummary(),
                messages,
                Collections.emptyList()
        );
    }
}

