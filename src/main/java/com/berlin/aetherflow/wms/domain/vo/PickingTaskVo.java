package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.PickingTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拣货任务 VO。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PickingTask.class, convertGenerate = false)
public class PickingTaskVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String taskNo;

    private String sourceType;

    private Long waveId;

    private String waveNo;

    private Long outboundOrderId;

    private String outboundOrderNo;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private String status;

    private BigDecimal totalQty;

    private BigDecimal pickedQty;

    private LocalDateTime pickingTime;

    private String remark;
}
