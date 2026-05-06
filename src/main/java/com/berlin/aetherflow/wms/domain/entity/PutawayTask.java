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
 * 上架任务实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("putaway_task")
public class PutawayTask extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 上架任务号。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String taskNo;

    /**
     * 入库单ID。
     */
    private Long inboundOrderId;

    /**
     * 入库单号。
     */
    private String inboundOrderNo;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 状态（PENDING/COMPLETED/CANCELLED）。
     */
    private String status;

    /**
     * 应上架数量。
     */
    private BigDecimal totalQty;

    /**
     * 已上架数量。
     */
    private BigDecimal completedQty;

    /**
     * 上架完成时间。
     */
    private LocalDateTime putawayTime;

    /**
     * 备注。
     */
    private String remark;
}
