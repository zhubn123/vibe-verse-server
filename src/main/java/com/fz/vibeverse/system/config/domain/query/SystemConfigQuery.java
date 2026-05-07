package com.fz.vibeverse.system.config.domain.query;

import com.fz.vibeverse.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统参数配置分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemConfigQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "配置键")
    @Size(max = 128, message = "配置键长度不能超过128位")
    @Pattern(regexp = "^[a-z0-9_.:-]*$", message = "配置键格式非法")
    private String configKey;

    @Schema(description = "配置名称")
    @Size(max = 128, message = "配置名称长度不能超过128位")
    private String configName;

    @Schema(description = "状态（0正常 1停用）")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;
}
