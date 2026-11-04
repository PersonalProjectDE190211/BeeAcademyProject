package com.beeacademy.backend.domain.support;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComplaintRepositoryPort {

    Complaint save(Complaint complaint);

    Optional<Complaint> findById(UUID id);

    List<Complaint> findBySenderId(UUID senderId);

    List<Complaint> findByStatus(ComplaintStatus status);

    List<Complaint> findByCategory(ComplaintCategory category);

    List<Complaint> findAllOrderByLastActivityDesc();
}
