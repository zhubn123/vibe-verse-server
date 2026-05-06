package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 上架任务查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PutawayTaskQuery extends PageQuery {

    private String taskNo;

    private Long inboundOrderId;

    private String inboundOrderNo;

    private Long warehouseId;

    private Long locationId;

    private Long materialId;

    private String batchNo;

    private String status;
}
