package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存效期预警查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryExpiryWarningQuery extends PageQuery {

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 区域ID。
     */
    private Long areaId;

    /**
     * 库位ID。
     */
    private Long locationId;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 批次号。
     */
    private String batchNo;

    /**
     * 临期天数，默认30天。
     */
    @Min(value = 0, message = "临期天数不能小于0")
    @Max(value = 365, message = "临期天数不能超过365")
    private Integer days = 30;
}
