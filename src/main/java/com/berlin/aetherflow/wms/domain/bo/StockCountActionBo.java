package com.berlin.aetherflow.wms.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 盘点单动作入参。
 */
@Data
public class StockCountActionBo {

    @NotBlank(message = "动作不能为空")
    @Size(max = 32, message = "动作长度不能超过32个字符")
    private String action;

    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
