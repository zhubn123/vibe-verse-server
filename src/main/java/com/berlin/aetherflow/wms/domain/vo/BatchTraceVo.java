package com.berlin.aetherflow.wms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 批次追溯分页返回对象。
 */
@Data
public class BatchTraceVo {

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String specification;

    private String unit;

    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private BigDecimal quantity;

    private BigDecimal lockedQuantity;

    private BigDecimal frozenQuantity;

    private BigDecimal availableQuantity;

    private Long locationCount;

    private Long transactionCount;

    private LocalDateTime earliestInboundTime;

    private LocalDateTime latestTransactionTime;
}
