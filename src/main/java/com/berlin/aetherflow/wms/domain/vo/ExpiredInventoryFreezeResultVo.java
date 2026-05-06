package com.berlin.aetherflow.wms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 过期库存冻结结果。
 */
@Data
public class ExpiredInventoryFreezeResultVo {

    /**
     * 扫描到的过期可用库存记录数。
     */
    private Integer scannedCount;

    /**
     * 成功冻结的库存记录数。
     */
    private Integer frozenCount;

    /**
     * 成功冻结总数量。
     */
    private BigDecimal frozenQuantity;

    /**
     * 跳过记录数。
     */
    private Integer skippedCount;

    /**
     * 失败记录数。
     */
    private Integer failedCount;

    /**
     * 执行时间。
     */
    private LocalDateTime executedAt;
}
