package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.common.BaseEntity;
import com.berlin.aetherflow.wms.domain.entity.WaveOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 波次列表返回对象。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = WaveOrder.class, convertGenerate = false)
public class WaveVo extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String waveNo;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private String status;

    private String groupRule;

    private Integer totalOrders;

    private Integer totalItems;

    private BigDecimal totalQty;

    private Integer orderCount;

    private Integer itemCount;

    private Long pickingTaskId;

    private String pickingTaskNo;

    private Boolean pickingGenerated;

    private LocalDateTime releaseTime;

    private LocalDateTime cancelTime;

    private String remark;
}
