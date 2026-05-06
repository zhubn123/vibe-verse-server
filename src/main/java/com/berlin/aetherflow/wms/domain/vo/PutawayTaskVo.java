package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.PutawayTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 上架任务 VO。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PutawayTask.class, convertGenerate = false)
public class PutawayTaskVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String taskNo;

    private Long inboundOrderId;

    private String inboundOrderNo;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private String status;

    private BigDecimal totalQty;

    private BigDecimal completedQty;

    private LocalDateTime putawayTime;

    private String remark;
}
