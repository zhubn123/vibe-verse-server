package com.berlin.aetherflow.wms.domain.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建盘点单入参。
 */
@Data
public class StockCountCreateBo {

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private Long areaId;

    private Long locationId;

    private Long materialId;

    @Size(max = 128, message = "批次号长度不能超过128个字符")
    private String batchNo;

    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
