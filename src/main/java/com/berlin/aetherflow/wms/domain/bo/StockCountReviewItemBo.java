package com.berlin.aetherflow.wms.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘点单复盘明细录入。
 */
@Data
public class StockCountReviewItemBo {

    @NotNull(message = "盘点明细ID不能为空")
    private Long id;

    @NotNull(message = "复盘实盘数量不能为空")
    @DecimalMin(value = "0.00", message = "复盘实盘数量不能小于0")
    private BigDecimal reviewCountedQty;

    @Size(max = 255, message = "差异原因长度不能超过255个字符")
    private String differenceReason;

    @Size(max = 255, message = "复盘备注长度不能超过255个字符")
    private String reviewRemark;
}
