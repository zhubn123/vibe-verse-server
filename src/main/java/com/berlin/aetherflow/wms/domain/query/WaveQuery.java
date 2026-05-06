package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 波次分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WaveQuery extends PageQuery {

    @Size(max = 64, message = "波次号长度不能超过64个字符")
    private String waveNo;

    private Long warehouseId;

    @Size(max = 16, message = "波次状态长度不能超过16个字符")
    private String status;

    @Size(max = 16, message = "波次分组规则长度不能超过16个字符")
    private String groupRule;

    private Long outboundOrderId;

    private Long materialId;

    private Long areaId;
}
