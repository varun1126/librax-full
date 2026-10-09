package com.librax.repository;

import com.librax.model.AuditLog;
import java.util.List;

public interface AuditLogRepository {
    void save(AuditLog auditLog);
    List<AuditLog> findAll();
    List<AuditLog> findRecent(int limit);
}
