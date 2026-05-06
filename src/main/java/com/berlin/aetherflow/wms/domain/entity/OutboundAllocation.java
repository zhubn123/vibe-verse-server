package com.berlin.aetherflow.wms.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
import java.time.LocalDateTime;

/**
 * 出库库存分配明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("outbound_allocation")
public class OutboundAllocation extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 出库单ID。
     */
    private Long orderId;

    /**
     * 出库单明细ID。
     */
    private Long orderItemId;

    /**
     * 行号。
     */
    private Integer lineNo;

    /**
     * 库存ID。
     */
    private Long inventoryId;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 库位ID。
     */
    private Long locationId;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 批次号。
     */
    private String batchNo;

    /**
     * 生产日期。
     */
    private LocalDate productionDate;

    /**
     * 到期日期。
     */
    private LocalDate expiryDate;

    /**
     * 分配数量。
     */
    private BigDecimal allocatedQty;

    /**
     * 状态（ACTIVE/RELEASED/CONSUMED）。
     */
    private String status;

    /**
     * 分配时间。
     */
    private LocalDateTime allocateTime;

    /**
     * 释放时间。
     */
    private LocalDateTime releaseTime;

    /**
     * 消费时间。
     */
    private LocalDateTime consumeTime;

    /**
     * 备注。
     */
    private String remark;
}
