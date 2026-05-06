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

/**
 * 拣货任务明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("picking_task_item")
public class PickingTaskItem extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 拣货任务ID。
     */
    private Long taskId;

    /**
     * 来源波次ID。
     */
    private Long waveId;

    /**
     * 来源波次明细ID。
     */
    private Long waveItemId;

    /**
     * 出库单明细ID。
     */
    private Long outboundOrderItemId;

    /**
     * 出库库存分配ID。
     */
    private Long allocationId;

    /**
     * 库存ID。
     */
    private Long inventoryId;

    /**
     * 行号。
     */
    private Integer lineNo;

    private Long warehouseId;

    private Long locationId;

    private Long materialId;

    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    /**
     * 应拣数量。
     */
    private BigDecimal plannedQty;

    /**
     * 已拣数量。
     */
    private BigDecimal pickedQty;

    /**
     * 状态（PENDING/COMPLETED/CANCELLED）。
     */
    private String status;

    /**
     * 备注。
     */
    private String remark;
}
