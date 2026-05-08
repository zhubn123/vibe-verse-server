package com.fz.vibeverse.system.dict.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典项保存参数。
 */
@Data
public class DictItemSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "字典项值")
    @NotBlank(message = "字典项值不能为空")
    @Size(max = 128, message = "字典项值长度不能超过128位")
    private String itemValue;

    @Schema(description = "字典项标签")
    @NotBlank(message = "字典项标签不能为空")
    @Size(max = 128, message = "字典项标签长度不能超过128位")
    private String itemLabel;

    @Schema(description = "排序号")
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能小于0")
    private Integer sortOrder;

    @Schema(description = "状态（1正常 0停用）")
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255位")
    private String remark;
}
