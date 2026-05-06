package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 盘点单查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StockCountQuery extends PageQuery {

    private String countNo;

    private Long warehouseId;

    private Long areaId;

    private Long locationId;

    private Long materialId;

    private String batchNo;

    private String status;
}
