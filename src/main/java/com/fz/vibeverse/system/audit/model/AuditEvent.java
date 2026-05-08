package com.fz.vibeverse.system.audit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计事件。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String module;
    private AuditType type;
    private String description;
    private String methodSignature;
    private String httpMethod;
    private String uri;
    private String ip;
    private Boolean success;
    private Long costMs;
    private String requestParams;
    private String resultSummary;
    private String errorMessage;
    private LocalDateTime occurTime;
}
