package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.StockCountItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 盘点单明细 VO。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = StockCountItem.class, convertGenerate = false)
public class StockCountItemVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long countId;

    private Long inventoryId;

    private Integer lineNo;

    private Long warehouseId;

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

    private BigDecimal expectedQty;

    private BigDecimal countedQty;

    private BigDecimal reviewCountedQty;

    private BigDecimal differenceQty;

    private String differenceReason;

    private String reviewRemark;

    private String remark;
}
