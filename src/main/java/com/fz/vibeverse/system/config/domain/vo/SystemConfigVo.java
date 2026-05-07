package com.fz.vibeverse.system.config.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统参数配置视图对象。
 */
@Data
public class SystemConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置名称")
    private String configName;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "值类型")
    private String valueType;

    @Schema(description = "状态（0正常 1停用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
