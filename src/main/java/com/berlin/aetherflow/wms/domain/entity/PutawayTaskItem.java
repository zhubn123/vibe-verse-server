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
 * 上架任务明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("putaway_task_item")
public class PutawayTaskItem extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 上架任务ID。
     */
    private Long taskId;

    /**
     * 入库单明细ID。
     */
    private Long inboundOrderItemId;

    /**
     * 行号。
     */
    private Integer lineNo;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 目标库位ID。
     */
    private Long locationId;

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
     * 应上架数量。
     */
    private BigDecimal plannedQty;

    /**
     * 已上架数量。
     */
    private BigDecimal completedQty;

    /**
     * 状态（PENDING/COMPLETED/CANCELLED）。
     */
    private String status;

    /**
     * 备注。
     */
    private String remark;
}
