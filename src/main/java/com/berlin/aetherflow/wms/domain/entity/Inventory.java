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
 * 库存实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("inventory")
public class Inventory extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

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
     * 首次入库时间。
     */
    private LocalDateTime inboundTime;

    /**
     * 当前库存。
     */
    private BigDecimal quantity;

    /**
     * 锁定库存。
     */
    private BigDecimal lockedQuantity;

    /**
     * 冻结库存。
     */
    private BigDecimal frozenQuantity;
}
