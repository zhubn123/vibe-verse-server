package com.berlin.aetherflow.wms.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘点单明细实盘录入。
 */
@Data
public class StockCountItemBo {

    @NotNull(message = "盘点明细ID不能为空")
    private Long id;

    @NotNull(message = "实盘数量不能为空")
    @DecimalMin(value = "0.00", message = "实盘数量不能小于0")
    private BigDecimal countedQty;

    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
