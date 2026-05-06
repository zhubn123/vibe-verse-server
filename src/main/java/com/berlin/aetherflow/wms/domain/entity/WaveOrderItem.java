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
 * 波次出库单明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("wave_order_item")
public class WaveOrderItem extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 波次ID。
     */
    private Long waveId;

    /**
     * 行号。
     */
    private Integer lineNo;

    /**
     * 出库单ID。
     */
    private Long outboundOrderId;

    /**
     * 出库单号。
     */
    private String outboundOrderNo;

    /**
     * 出库单明细ID。
     */
    private Long outboundOrderItemId;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 区域ID。
     */
    private Long areaId;

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
     * 计划数量。
     */
    private BigDecimal plannedQty;

    /**
     * 状态（DRAFT/RELEASED/CANCELLED）。
     */
    private String status;

    /**
     * 备注。
     */
    private String remark;
}
