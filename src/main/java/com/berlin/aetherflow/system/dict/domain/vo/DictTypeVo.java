package com.berlin.aetherflow.system.dict.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 字典类型视图对象。
 */
@Data
public class DictTypeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典名称")
    private String dictName;

    @Schema(description = "所属模块")
    private String module;

    @Schema(description = "状态（0正常 1停用）")
    private Integer status;

    @Schema(description = "字典项数量")
    private Long itemCount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "字典项列表")
    private List<DictItemVo> items;
}
