package com.berlin.aetherflow.wms.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrderItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 出库单明细实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = OutboundOrderItem.class, convertGenerate = false)
public class OutboundOrderItemVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 出库单ID。
     */
    private Long orderId;

    /**
     * 行号。
     */
    private Integer lineNo;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 物料编码。
     */
    private String materialCode;

    /**
     * 物料名称。
     */
    private String materialName;

    /**
     * 来源库位ID。
     */
    private Long locationId;

    /**
     * 来源库位编码。
     */
    private String locationCode;

    /**
     * 来源库位名称。
     */
    private String locationName;

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
     * 计划出库数量。
     */
    private BigDecimal plannedQty;

    /**
     * 已出库数量。
     */
    private BigDecimal shippedQty;

    /**
     * 备注。
     */
    private String remark;
}
