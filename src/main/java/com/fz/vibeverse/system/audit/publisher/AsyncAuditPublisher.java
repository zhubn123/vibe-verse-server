package com.fz.vibeverse.system.audit.publisher;

import com.fz.vibeverse.system.audit.model.AuditEvent;
import com.fz.vibeverse.system.audit.persistence.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 默认方案：线程池异步直写数据库。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "vibe-verse.audit", name = "transport", havingValue = "async", matchIfMissing = true)
public class AsyncAuditPublisher implements AuditPublisher {

    private final AuditLogMapper auditLogMapper;

    @Override
    @Async("auditExecutor")
    public void publish(AuditEvent event) {
        try {
            auditLogMapper.insert(event);
        } catch (Exception ex) {
            log.error("[AUDIT] 异步写库失败, module={}, type={}", event.getModule(), event.getType(), ex);
        }
    }
}
