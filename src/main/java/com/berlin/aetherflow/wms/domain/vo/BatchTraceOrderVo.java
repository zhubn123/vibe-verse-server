package com.berlin.aetherflow.wms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 批次追溯关联单据线索返回对象。
 */
@Data
public class BatchTraceOrderVo {

    private String orderType;

    private String orderTypeLabel;

    private String direction;

    private String directionLabel;

    private Long orderId;

    private String orderNo;

    private Long itemId;

    private Integer lineNo;

    private String status;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private Long areaId;

    private String areaCode;

    private String areaName;

    private Long locationId;

    private String locationCode;

    private String locationName;

    private Long sourceLocationId;

    private String sourceLocationCode;

    private String sourceLocationName;

    private Long targetLocationId;

    private String targetLocationCode;

    private String targetLocationName;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String specification;

    private String unit;

    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private BigDecimal plannedQuantity;

    private BigDecimal actualQuantity;

    private BigDecimal differenceQuantity;

    private LocalDateTime businessTime;

    private String remark;
}
