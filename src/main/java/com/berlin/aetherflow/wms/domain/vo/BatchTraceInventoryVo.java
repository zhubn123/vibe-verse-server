package com.berlin.aetherflow.wms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 批次追溯当前库存位置返回对象。
 */
@Data
public class BatchTraceInventoryVo {

    private Long inventoryId;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private Long areaId;

    private String areaCode;

    private String areaName;

    private Long locationId;

    private String locationCode;

    private String locationName;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String specification;

    private String unit;

    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private LocalDateTime inboundTime;

    private BigDecimal quantity;

    private BigDecimal lockedQuantity;

    private BigDecimal frozenQuantity;

    private BigDecimal availableQuantity;
}
