package com.berlin.aetherflow.wms.domain.vo;

import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Outbound order detail response.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = OutboundOrder.class, convertGenerate = false)
public class OutboundOrderDetailVo extends OutboundOrderVo {

    private List<OutboundOrderItemVo> orderItems;
}
