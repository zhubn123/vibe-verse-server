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
 * 拣货任务实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("picking_task")
public class PickingTask extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 拣货任务号。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String taskNo;

    /**
     * 来源类型（OUTBOUND_ORDER/WAVE）。
     */
    private String sourceType;

    /**
     * 来源波次ID。
     */
    private Long waveId;

    /**
     * 来源波次号。
     */
    private String waveNo;

    /**
     * 出库单ID。
     */
    private Long outboundOrderId;

    /**
     * 出库单号。
     */
    private String outboundOrderNo;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 状态（PENDING/COMPLETED/CANCELLED）。
     */
    private String status;

    /**
     * 应拣数量。
     */
    private BigDecimal totalQty;

    /**
     * 已拣数量。
     */
    private BigDecimal pickedQty;

    /**
     * 拣货完成时间。
     */
    private LocalDateTime pickingTime;

    /**
     * 备注。
     */
    private String remark;
}
