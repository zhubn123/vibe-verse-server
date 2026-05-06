package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.PickingTaskItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 拣货任务明细 VO。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PickingTaskItem.class, convertGenerate = false)
public class PickingTaskItemVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long taskId;

    private Long waveId;

    private Long waveItemId;

    private Long outboundOrderItemId;

    private Long allocationId;

    private Long inventoryId;

    private Integer lineNo;

    private Long warehouseId;

    private Long locationId;

    private String locationCode;

    private String locationName;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private BigDecimal plannedQty;

    private BigDecimal pickedQty;

    private String status;

    private String remark;
}
