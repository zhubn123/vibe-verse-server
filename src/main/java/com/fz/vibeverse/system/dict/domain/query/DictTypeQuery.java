package com.fz.vibeverse.system.dict.domain.query;

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
 * 字典类型分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictTypeQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "字典编码")
    @Size(max = 128, message = "字典编码长度不能超过128位")
    @Pattern(regexp = "^[A-Za-z0-9_:-]*$", message = "字典编码格式非法")
    private String dictCode;

    @Schema(description = "字典名称")
    @Size(max = 128, message = "字典名称长度不能超过128位")
    private String dictName;

    @Schema(description = "所属模块")
    @Size(max = 64, message = "所属模块长度不能超过64位")
    @Pattern(regexp = "^[A-Za-z0-9_-]*$", message = "所属模块格式非法")
    private String module;

    @Schema(description = "状态（1正常 0停用）")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;
}
