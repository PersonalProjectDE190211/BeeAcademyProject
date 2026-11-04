package com.beeacademy.backend.domain.family;

import java.util.List;
import java.util.UUID;

public interface ParentProgressAccessAuditRepositoryPort {

    ParentProgressAccessAudit save(ParentProgressAccessAudit audit);

    List<ParentProgressAccessAudit> findByStudentId(UUID studentId);

    List<ParentProgressAccessAudit> findByParentId(UUID parentId);
}
