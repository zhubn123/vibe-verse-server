package com.fz.vibeverse.system.config.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统参数配置保存参数。
 */
@Data
public class SystemConfigSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "配置键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置键不能为空")
    @Size(max = 128, message = "配置键长度不能超过128位")
    @Pattern(regexp = "^[a-z][a-z0-9_.:-]{0,127}$", message = "配置键必须以小写字母开头，且仅支持小写字母、数字、_、.、:、-")
    private String configKey;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 128, message = "配置名称长度不能超过128位")
    private String configName;

    @Schema(description = "配置值")
    @Size(max = 1024, message = "配置值长度不能超过1024位")
    private String configValue;

    @Schema(description = "值类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "值类型不能为空")
    @Size(max = 32, message = "值类型长度不能超过32位")
    @Pattern(regexp = "^[a-z][a-z0-9_-]{0,31}$", message = "值类型必须以小写字母开头，且仅支持小写字母、数字、_、-")
    private String valueType;

    @Schema(description = "状态（0正常 1停用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255位")
    private String remark;
}
