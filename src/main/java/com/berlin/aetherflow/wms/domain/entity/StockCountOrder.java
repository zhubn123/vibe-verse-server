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
import java.time.LocalDateTime;

/**
 * 盘点单实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("stock_count_order")
public class StockCountOrder extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 盘点单号。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String countNo;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 状态（PENDING/REVIEWING/APPROVED/ADJUSTED/CANCELLED）。
     */
    private String status;

    /**
     * 明细总数。
     */
    private Integer totalItems;

    /**
     * 差异明细数。
     */
    private Integer differenceItems;

    /**
     * 账面总数量。
     */
    private BigDecimal expectedQty;

    /**
     * 实盘总数量。
     */
    private BigDecimal countedQty;

    /**
     * 差异总数量。
     */
    private BigDecimal differenceQty;

    /**
     * 盘点时间。
     */
    private LocalDateTime countTime;

    /**
     * 提交复盘时间。
     */
    private LocalDateTime reviewSubmitTime;

    /**
     * 复盘审批时间。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime reviewTime;

    /**
     * 复盘审批人。
     */
    private String reviewBy;

    /**
     * 复盘审批备注。
     */
    private String reviewRemark;

    /**
     * 调账时间。
     */
    private LocalDateTime adjustTime;

    /**
     * 备注。
     */
    private String remark;
}
