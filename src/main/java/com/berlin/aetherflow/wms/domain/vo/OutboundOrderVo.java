package com.berlin.aetherflow.wms.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出库单实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = OutboundOrder.class, convertGenerate = false)
public class OutboundOrderVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 出库单号。
     */
    private String orderNo;

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 仓库编码。
     */
    private String warehouseCode;

    /**
     * 仓库名称。
     */
    private String warehouseName;

    /**
     * 状态（0草稿 1已确认）。
     */
    private Integer status;

    /**
     * 库存分配状态（UNALLOCATED/ACTIVE/RELEASED/CONSUMED）。
     */
    private String allocationStatus;

    /**
     * 库存分配状态文案。
     */
    private String allocationStatusLabel;

    /**
     * 当前分配状态对应的数量。
     */
    private BigDecimal allocatedQty;

    /**
     * 实际出库时间。
     */
    private LocalDateTime outboundTime;

    /**
     * 备注。
     */
    private String remark;
}
