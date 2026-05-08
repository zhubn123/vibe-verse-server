package com.fz.vibeverse.system.dict.domain.bo;

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
 * 字典类型保存参数。
 */
@Data
public class DictTypeSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "字典编码")
    @NotBlank(message = "字典编码不能为空")
    @Size(max = 128, message = "字典编码长度不能超过128位")
    @Pattern(regexp = "^[A-Za-z0-9_:-]+$", message = "字典编码格式非法")
    private String dictCode;

    @Schema(description = "字典名称")
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 128, message = "字典名称长度不能超过128位")
    private String dictName;

    @Schema(description = "所属模块")
    @NotBlank(message = "所属模块不能为空")
    @Size(max = 64, message = "所属模块长度不能超过64位")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "所属模块格式非法")
    private String module;

    @Schema(description = "状态（1正常 0停用）")
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255位")
    private String remark;
}
