package com.librax.logging;

import com.librax.model.AuditLog;
import com.librax.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes audit trail entries to the audit_logs table and mirrors a short
 * summary to SLF4J. Services call this after any sensitive operation
 * (BOOK_CREATED, BOOK_ISSUED, LOGIN_SUCCESS, etc.) — see RULES.md §12 and
 * PHASES.md.
 */
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    private final AuditLogRepository repository;

    public AuditLogger(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(Integer userId, String action, String entity, Integer entityId, String description) {
        AuditLog entry = new AuditLog(userId, action, entity, entityId, description);
        repository.save(entry);
        log.info("AUDIT [{}] {} {} #{} - {}", action, entity, entity, entityId, description);
    }
}
