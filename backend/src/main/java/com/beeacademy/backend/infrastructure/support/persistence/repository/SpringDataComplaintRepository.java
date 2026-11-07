package com.beeacademy.backend.infrastructure.support.persistence.repository;

import com.beeacademy.backend.infrastructure.support.persistence.entity.ComplaintJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataComplaintRepository extends JpaRepository<ComplaintJpaEntity, UUID> {

    List<ComplaintJpaEntity> findBySenderIdOrderByLastActivityAtDesc(UUID senderId);

    List<ComplaintJpaEntity> findByStatusOrderByLastActivityAtDesc(String status);

    List<ComplaintJpaEntity> findByCategoryOrderByLastActivityAtDesc(String category);

    List<ComplaintJpaEntity> findAllByOrderByLastActivityAtDesc();
}

