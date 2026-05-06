package com.berlin.aetherflow.wms.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.berlin.aetherflow.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 盘点单明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("stock_count_item")
public class StockCountItem extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 盘点单ID。
     */
    private Long countId;

    /**
     * 库存ID。
     */
    private Long inventoryId;

    /**
     * 行号。
     */
    private Integer lineNo;

    private Long warehouseId;

    private Long areaId;

    private Long locationId;

    private Long materialId;

    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    /**
     * 账面数量。
     */
    private BigDecimal expectedQty;

    /**
     * 实盘数量。
     */
    private BigDecimal countedQty;

    /**
     * 复盘实盘数量。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private BigDecimal reviewCountedQty;

    /**
     * 差异数量。
     */
    private BigDecimal differenceQty;

    /**
     * 差异原因。
     */
    private String differenceReason;

    /**
     * 复盘备注。
     */
    private String reviewRemark;

    /**
     * 备注。
     */
    private String remark;
}
