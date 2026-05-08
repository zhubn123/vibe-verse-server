package com.fz.vibeverse.system.audit.persistence;

import com.fz.vibeverse.system.audit.model.AuditEvent;
import com.fz.vibeverse.system.user.domain.entity.SysAuditLog;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 审计事件到系统审计日志实体的转换器。
 */
public final class AuditLogEntity {

    private AuditLogEntity() {
    }

    public static SysAuditLog from(AuditEvent event) {
        SysAuditLog log = new SysAuditLog();
        log.setUserId(event == null ? null : event.getUserId());
        log.setUsername(truncate(normalize(event == null ? null : event.getUsername()), 64));
        log.setEventType(truncate(resolveEventType(event), 32));
        log.setEventName(truncate(resolveEventName(event), 64));
        log.setRequestUri(truncate(normalize(event == null ? null : event.getUri()), 255));
        log.setClientIp(truncate(normalize(event == null ? null : event.getIp()), 64));
        log.setResult(Boolean.TRUE.equals(event == null ? null : event.getSuccess()) ? 1 : 0);
        log.setMessage(truncate(buildMessage(event), 255));
        log.setOccurTime(event == null || event.getOccurTime() == null ? LocalDateTime.now() : event.getOccurTime());
        return log;
    }

    private static String resolveEventType(AuditEvent event) {
        if (event == null) {
            return null;
        }
        if (StringUtils.isNotBlank(event.getModule())) {
            return event.getModule().trim().toUpperCase(Locale.ROOT);
        }
        return event.getType() == null ? null : event.getType().name();
    }

    private static String resolveEventName(AuditEvent event) {
        if (event == null) {
            return null;
        }
        if (StringUtils.isNotBlank(event.getDescription())) {
            return event.getDescription().trim();
        }
        return event.getType() == null ? null : event.getType().getLabel();
    }

    private static String buildMessage(AuditEvent event) {
        if (event == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        if (StringUtils.isNotBlank(event.getHttpMethod())) {
            parts.add(event.getHttpMethod());
        }
        if (StringUtils.isNotBlank(event.getMethodSignature())) {
            parts.add(event.getMethodSignature());
        }
        if (event.getCostMs() != null) {
            parts.add("cost=" + event.getCostMs() + "ms");
        }
        if (StringUtils.isNotBlank(event.getRequestParams())) {
            parts.add("params=" + event.getRequestParams());
        }
        if (StringUtils.isNotBlank(event.getResultSummary())) {
            parts.add("result=" + event.getResultSummary());
        }
        if (StringUtils.isNotBlank(event.getErrorMessage())) {
            parts.add("error=" + event.getErrorMessage());
        }
        return parts.isEmpty() ? null : String.join(" | ", parts);
    }

    private static String normalize(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
