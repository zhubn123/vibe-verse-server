package com.berlin.aetherflow.wms.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.TransferOrderItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 移库单明细返回对象。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = TransferOrderItem.class, convertGenerate = false)
public class TransferOrderItemVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long orderId;

    private Integer lineNo;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private Long sourceLocationId;

    private String sourceLocationCode;

    private String sourceLocationName;

    private Long targetLocationId;

    private String targetLocationCode;

    private String targetLocationName;

    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private BigDecimal transferQty;

    private String remark;
}
