package com.berlin.aetherflow.wms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 批次追溯库存流水返回对象。
 */
@Data
public class BatchTraceTransactionVo {

    private Long id;

    private String bizType;

    private Long bizId;

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

    private BigDecimal changeQty;

    private BigDecimal beforeQty;

    private BigDecimal afterQty;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime operateTime;

    private String remark;
}
