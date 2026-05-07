package com.fz.vibeverse.system.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计日志分页返回对象。
 */
@Data
public class AuditLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "日志 ID")
    private Long id;

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "事件名称")
    private String eventName;

    @Schema(description = "请求路径")
    private String requestUri;

    @Schema(description = "客户端 IP")
    private String clientIp;

    @Schema(description = "执行结果（1成功 0失败）")
    private Integer result;

    @Schema(description = "结果消息")
    private String message;

    @Schema(description = "事件时间")
    private LocalDateTime occurTime;
}
