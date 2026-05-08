package com.fz.vibeverse.system.audit.publisher;

import com.fz.vibeverse.system.audit.model.AuditEvent;

/**
 * 审计事件发布器。
 */
public interface AuditPublisher {

    void publish(AuditEvent event);
}
