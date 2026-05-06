package com.berlin.aetherflow.wms.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存冻结/解冻参数。
 */
@Data
public class StockFreezeBo {

    /**
     * 操作数量。
     */
    @NotNull(message = "操作数量不能为空", groups = {Default.class})
    @DecimalMin(value = "0.01", message = "操作数量必须大于0", groups = {Default.class})
    private BigDecimal quantity;

    /**
     * 冻结类型。
     */
    @NotBlank(message = "冻结类型不能为空", groups = {Default.class})
    @Size(max = 32, message = "冻结类型长度不能超过32个字符", groups = {Default.class})
    private String freezeType;

    /**
     * 原因说明。
     */
    @Size(max = 255, message = "冻结原因长度不能超过255个字符", groups = {Default.class})
    private String reason;
}
