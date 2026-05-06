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
 * 波次单实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("wave_order")
public class WaveOrder extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 波次号。
     */
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String waveNo;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 状态（DRAFT/RELEASED/CANCELLED）。
     */
    private String status;

    /**
     * 分组规则（MANUAL/BY_ORDER/BY_SKU/BY_AREA）。
     */
    private String groupRule;

    /**
     * 出库单数。
     */
    private Integer totalOrders;

    /**
     * 明细行数。
     */
    private Integer totalItems;

    /**
     * 计划数量。
     */
    private BigDecimal totalQty;

    /**
     * 发布时间。
     */
    private LocalDateTime releaseTime;

    /**
     * 取消时间。
     */
    private LocalDateTime cancelTime;

    /**
     * 备注。
     */
    private String remark;
}
