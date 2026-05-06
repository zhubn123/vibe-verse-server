package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.StockCountOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 盘点单 VO。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = StockCountOrder.class, convertGenerate = false)
public class StockCountVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String countNo;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private String status;

    private Integer totalItems;

    private Integer differenceItems;

    private BigDecimal expectedQty;

    private BigDecimal countedQty;

    private BigDecimal differenceQty;

    private LocalDateTime countTime;

    private LocalDateTime reviewSubmitTime;

    private LocalDateTime reviewTime;

    private String reviewBy;

    private String reviewRemark;

    private LocalDateTime adjustTime;

    private String remark;
}
