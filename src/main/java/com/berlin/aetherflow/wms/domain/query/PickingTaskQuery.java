package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拣货任务查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PickingTaskQuery extends PageQuery {

    private String taskNo;

    private String sourceType;

    private Long waveId;

    private String waveNo;

    private Long outboundOrderId;

    private String outboundOrderNo;

    private Long warehouseId;

    private Long locationId;

    private Long materialId;

    private String batchNo;

    private String status;
}
