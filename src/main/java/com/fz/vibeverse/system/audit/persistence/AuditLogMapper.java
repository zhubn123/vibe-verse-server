package com.fz.vibeverse.system.audit.persistence;

import com.fz.vibeverse.system.audit.model.AuditEvent;
import com.fz.vibeverse.system.user.mapper.SysAuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审计落库适配器。
 */
@Component
@RequiredArgsConstructor
public class AuditLogMapper {

    private final SysAuditLogMapper sysAuditLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void insert(AuditEvent event) {
        sysAuditLogMapper.insert(AuditLogEntity.from(event));
    }
}
