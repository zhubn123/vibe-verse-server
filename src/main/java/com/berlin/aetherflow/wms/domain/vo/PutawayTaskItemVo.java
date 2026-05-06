package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.PutawayTaskItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 上架任务明细 VO。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PutawayTaskItem.class, convertGenerate = false)
public class PutawayTaskItemVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long taskId;

    private Long inboundOrderItemId;

    private Integer lineNo;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private Long locationId;

    private String locationCode;

    private String locationName;

    private String batchNo;

    private LocalDate productionDate;

    private LocalDate expiryDate;

    private BigDecimal plannedQty;

    private BigDecimal completedQty;

    private String status;

    private String remark;
}
