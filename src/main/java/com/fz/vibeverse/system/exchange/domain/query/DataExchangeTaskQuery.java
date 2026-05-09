package com.fz.vibeverse.system.exchange.domain.query;

import com.fz.vibeverse.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 导入导出任务分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataExchangeTaskQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "方向：IMPORT / EXPORT")
    @Pattern(regexp = "^$|IMPORT|EXPORT", message = "任务方向非法")
    private String direction;

    @Schema(description = "场景标识")
    @Size(max = 64, message = "场景标识长度不能超过64位")
    @Pattern(regexp = "^[a-zA-Z0-9_.:-]*$", message = "场景标识格式非法")
    private String scene;

    @Schema(description = "状态：PENDING / RUNNING / SUCCESS / FAILED")
    @Pattern(regexp = "^$|PENDING|RUNNING|SUCCESS|FAILED", message = "任务状态非法")
    private String status;
}
