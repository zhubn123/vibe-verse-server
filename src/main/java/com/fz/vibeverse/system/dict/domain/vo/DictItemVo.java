package com.fz.vibeverse.system.dict.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典项视图对象。
 */
@Data
public class DictItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典项值")
    private String value;

    @Schema(description = "字典项标签")
    private String label;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态（1正常 0停用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
