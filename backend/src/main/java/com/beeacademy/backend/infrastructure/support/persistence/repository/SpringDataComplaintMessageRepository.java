package com.beeacademy.backend.infrastructure.support.persistence.repository;

import com.beeacademy.backend.infrastructure.support.persistence.entity.ComplaintMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataComplaintMessageRepository extends JpaRepository<ComplaintMessageJpaEntity, UUID> {

    List<ComplaintMessageJpaEntity> findByComplaintIdOrderByCreatedAtAsc(UUID complaintId);
}

