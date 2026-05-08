package com.fz.vibeverse.system.audit.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fz.vibeverse.system.audit.annotation.AuditLog;
import com.fz.vibeverse.system.audit.config.AuditProperties;
import com.fz.vibeverse.system.audit.model.AuditEvent;
import com.fz.vibeverse.system.audit.publisher.AuditPublisher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 审计日志切面。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "vibe-verse.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditLogAspect {

    private static final Set<String> DEFAULT_EXCLUDED_PARAM_NAMES = Set.of(
            "password",
            "oldPassword",
            "newPassword",
            "confirmPassword",
            "refreshToken",
            "token",
            "authorization"
    );

    private final AuditPublisher auditPublisher;
    private final AuditProperties auditProperties;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        if (!auditProperties.isEnabled()) {
            return joinPoint.proceed();
        }

        long startedAt = System.currentTimeMillis();
        HttpServletRequest request = getCurrentRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        AuditEvent.AuditEventBuilder builder = AuditEvent.builder()
                .userId(resolveCurrentUserId())
                .username(resolveCurrentUsername())
                .module(normalizeModule(auditLog.module()))
                .type(auditLog.type())
                .description(resolveDescription(auditLog))
                .methodSignature(signature.getDeclaringType().getSimpleName() + "." + signature.getName())
                .httpMethod(request == null ? null : request.getMethod())
                .uri(request == null ? null : request.getRequestURI())
                .ip(resolveIp(request))
                .occurTime(LocalDateTime.now());

        if (auditLog.logParams()) {
            builder.requestParams(truncate(serializeParams(signature, joinPoint.getArgs(), auditLog), auditLog.truncateLength()));
        }

        try {
            Object result = joinPoint.proceed();
            builder.success(true);
            if (auditLog.logResult() && result != null) {
                builder.resultSummary(truncate(writeJson(result), auditLog.truncateLength()));
            }
            return result;
        } catch (Throwable ex) {
            builder.success(false);
            builder.errorMessage(truncate(ex.getMessage(), auditLog.truncateLength()));
            throw ex;
        } finally {
            builder.costMs(System.currentTimeMillis() - startedAt);
            try {
                auditPublisher.publish(builder.build());
            } catch (Exception publishEx) {
                log.error("[AUDIT] 发布审计事件失败, method={}", signature.getName(), publishEx);
            }
        }
    }

    private HttpServletRequest getCurrentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private Long resolveCurrentUserId() {
        try {
            return StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String resolveCurrentUsername() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            Object operatorName = StpUtil.getTokenSession().get("operatorName");
            if (operatorName instanceof String value && StringUtils.isNotBlank(value)) {
                return value;
            }
            Long userId = StpUtil.getLoginIdAsLong();
            return userId == null ? null : String.valueOf(userId);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        return StringUtils.isNotBlank(ip) ? ip : request.getRemoteAddr();
    }

    private String serializeParams(MethodSignature signature, Object[] args, AuditLog auditLog) {
        if (args == null || args.length == 0) {
            return null;
        }
        String[] parameterNames = signature.getParameterNames();
        if (parameterNames == null || parameterNames.length == 0) {
            return null;
        }

        Set<String> excluded = new LinkedHashSet<>(DEFAULT_EXCLUDED_PARAM_NAMES);
        excluded.addAll(Arrays.stream(auditLog.excludeParamNames())
                .filter(StringUtils::isNotBlank)
                .map(name -> name.toLowerCase(java.util.Locale.ROOT))
                .toList());

        Map<String, Object> serialized = new LinkedHashMap<>();
        for (int index = 0; index < args.length && index < parameterNames.length; index++) {
            Object arg = args[index];
            if (shouldFilterObject(arg)) {
                continue;
            }
            String parameterName = parameterNames[index];
            if (StringUtils.isBlank(parameterName)) {
                continue;
            }
            if (excluded.contains(parameterName.toLowerCase(java.util.Locale.ROOT))) {
                serialized.put(parameterName, "***");
                continue;
            }
            serialized.put(parameterName, safeValue(arg));
        }
        if (serialized.isEmpty()) {
            return null;
        }
        return writeJson(serialized);
    }

    private Object safeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        try {
            return objectMapper.readValue(writeJson(value), Object.class);
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }

    private boolean shouldFilterObject(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof MultipartFile || value instanceof HttpServletRequest
                || value instanceof HttpServletResponse || value instanceof BindingResult) {
            return true;
        }
        Class<?> type = value.getClass();
        if (type.isArray()) {
            return MultipartFile.class.isAssignableFrom(type.getComponentType());
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().filter(Objects::nonNull).anyMatch(MultipartFile.class::isInstance);
        }
        if (value instanceof Map<?, ?> map) {
            return map.values().stream().filter(Objects::nonNull).anyMatch(MultipartFile.class::isInstance);
        }
        return false;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }

    private String normalizeModule(String module) {
        return StringUtils.isBlank(module) ? "SYSTEM" : module.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String resolveDescription(AuditLog auditLog) {
        if (StringUtils.isNotBlank(auditLog.description())) {
            return auditLog.description().trim();
        }
        return auditLog.type().getLabel();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
