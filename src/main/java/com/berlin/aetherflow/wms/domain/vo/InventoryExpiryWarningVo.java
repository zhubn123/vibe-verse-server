package com.berlin.aetherflow.wms.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存效期预警返回对象。
 */
@Data
@NoArgsConstructor
public class InventoryExpiryWarningVo {

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

    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private LocalDateTime inboundTime;

    private BigDecimal quantity;

    private BigDecimal lockedQuantity;

    private BigDecimal frozenQuantity;

    private BigDecimal availableQuantity;

    private Long daysToExpiry;

    private String level;

    private String levelLabel;
}
