package com.fz.vibeverse.system.user.domain.query;

import com.fz.vibeverse.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 审计日志分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLogQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名（精确匹配）
     */
    @Schema(description = "用户名（精确匹配）")
    @Size(max = 64, message = "用户名长度不能超过64位")
    private String username;

    /**
     * 事件类型（精确匹配）
     */
    @Schema(description = "事件类型（精确匹配）")
    @Size(max = 32, message = "事件类型长度不能超过32位")
    @Pattern(regexp = "^[A-Za-z0-9_:-]*$", message = "事件类型格式非法")
    private String eventType;

    /**
     * 事件名称（模糊匹配）
     */
    @Schema(description = "事件名称（模糊匹配）")
    @Size(max = 64, message = "事件名称长度不能超过64位")
    private String eventName;

    /**
     * 执行结果（1成功 0失败）
     */
    @Schema(description = "执行结果（1成功 0失败）")
    @Min(value = 0, message = "执行结果值非法")
    @Max(value = 1, message = "执行结果值非法")
    private Integer result;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
