package com.beeacademy.backend.domain.family;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParentStudentLinkRepositoryPort {

    ParentStudentLink save(ParentStudentLink link);

    Optional<ParentStudentLink> findById(UUID id);

    Optional<ParentStudentLink> findByInviteToken(String token);

    Optional<ParentStudentLink> findByParentIdAndStudentId(UUID parentId, UUID studentId);

    List<ParentStudentLink> findByParentId(UUID parentId);

    List<ParentStudentLink> findActiveLinksByParentId(UUID parentId);

    List<ParentStudentLink> findByStudentId(UUID studentId);

    boolean existsActiveLink(UUID parentId, UUID studentId);
}
