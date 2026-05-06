package com.berlin.aetherflow.wms.domain.query;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批次追溯详情查询对象。
 */
@Data
public class BatchTraceDetailQuery {

    /**
     * 仓库ID，可选。不传时聚合所有仓库。
     */
    private Long warehouseId;

    /**
     * 物料ID。
     */
    @NotNull(message = "物料不能为空")
    private Long materialId;

    /**
     * 批次号。空批次请传空字符串。
     */
    @NotNull(message = "批次号参数不能为空，空批次请传空字符串")
    private String batchNo;
}
